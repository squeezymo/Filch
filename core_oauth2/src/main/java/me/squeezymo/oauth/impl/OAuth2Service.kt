package me.squeezymo.oauth.impl

import android.content.Intent
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultLauncher
import androidx.browser.customtabs.CustomTabsIntent
import androidx.browser.customtabs.CustomTabsSession
import androidx.lifecycle.*
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import me.squeezymo.core.domain.data.StreamingServiceID
import me.squeezymo.oauth.api.IOAuth2Service
import me.squeezymo.oauth.api.OAuth2Config
import me.squeezymo.oauth.api.OAuth2State
import me.squeezymo.oauth.impl.storage.AuthStateStorage
import net.openid.appauth.*
import net.openid.appauth.connectivity.DefaultConnectionBuilder
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

internal class OAuth2Service @AssistedInject constructor(
    @Assisted override val serviceId: StreamingServiceID,
    @Assisted private val config: OAuth2Config,
    private val authStateStorage: AuthStateStorage
) : IOAuth2Service {

    private val authService = MutableStateFlow<AuthorizationService?>(null)
    private val authStateKey: String = "as_$serviceId"
    private var authState: AuthState =
        authStateStorage.readAuthState(authStateKey) ?: AuthState()
    private var authServiceConfiguration: AuthorizationServiceConfiguration? = null
    private var session: CustomTabsSession? = null

    override val authStateFlow = MutableStateFlow(authState.toOAuth2State())

    override fun bind(
        activity: ComponentActivity
    ) {
        activity.lifecycle.addObserver(
            object : DefaultLifecycleObserver {
                override fun onStart(owner: LifecycleOwner) {
                    authService.value = AuthorizationService(activity)
                }

                override fun onStop(owner: LifecycleOwner) {
                    authService.value?.dispose()
                    authService.value = null
                }
            }
        )

        activity.lifecycleScope.launch {
            activity.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch(Dispatchers.IO) {
                    runCatching {
                        val authServiceConfig = fetchAuthorizationServiceConfiguration()
                        authServiceConfig.authorizationEndpoint
                    }
                }
            }
        }
    }

    override fun notifyOnCustomTabsSession(
        session: CustomTabsSession
    ) {
        this.session = session
        prepareCustomTabs()
    }

    private fun prepareCustomTabs() {
        val currentSession = session
        val currentAuthServiceConfiguration = authServiceConfiguration

        if (currentSession != null && currentAuthServiceConfiguration != null) {
            currentSession.mayLaunchUrl(
                currentAuthServiceConfiguration.authorizationEndpoint,
                null,
                null
            )
        }
    }

    private suspend fun waitForAuthService(): AuthorizationService {
        return authService
            .filterNotNull()
            .first()
    }

    override suspend fun getFreshAccessState(): OAuth2State? {
        val authService = waitForAuthService()

        return suspendCoroutine { cont ->
            authState.performActionWithFreshTokens(authService) { _, _, _ ->
                authState.andThenStore()
                cont.resume(authState.toOAuth2State())
            }
        }
    }

    override suspend fun performAuthorizationRequest(
        activityResultLauncher: ActivityResultLauncher<Intent>,
        customTabsIntent: CustomTabsIntent
    ) {
        val authService = waitForAuthService()
        val authServiceConfig = fetchAuthorizationServiceConfiguration()

        activityResultLauncher.launch(
            authService
                .getAuthorizationRequestIntent(
                    AuthorizationRequest
                        .Builder(
                            authServiceConfig,
                            config.clientId,
                            config.authResponseType.value,
                            config.redirectUri
                        )
                        .let { builder ->
                            if (config.additionalAuthParameters.isNullOrEmpty()) {
                                builder
                            } else {
                                builder
                                    .setAdditionalParameters(config.additionalAuthParameters)
                            }
                        }
                        .setScopes(*config.scopes)
                        .build(),
                    customTabsIntent
                )
        )
    }

    override suspend fun handleAuthorizationResult(intent: Intent): OAuth2State? {
        val authResponse = AuthorizationResponse.fromIntent(intent)
        val authException = AuthorizationException.fromIntent(intent)

        if (authResponse == null && authException == null) {
            return null
        }

        authState.andThenStore { update(authResponse, authException) }

        if (authException != null) {
            throw authException
        }

        return if (config.authResponseType == OAuth2Config.AuthResponseType.CODE) {
            exchangeCodeForAccessToken(waitForAuthService(), authResponse!!)
        } else {
            authState.toOAuth2State()
        }
    }

    private suspend fun fetchAuthorizationServiceConfiguration(): AuthorizationServiceConfiguration {
        return authServiceConfiguration ?: when (val authServiceConfig = config.authServiceConfig) {
            is OAuth2Config.AuthServiceConfiguration.Manual -> {
                AuthorizationServiceConfiguration(
                    authServiceConfig.authUri,
                    authServiceConfig.tokenUri
                )
            }
            is OAuth2Config.AuthServiceConfiguration.OpenIdConnectIssuer -> {
                authState.authorizationServiceConfiguration
                    ?: suspendCoroutine { cont ->
                        AuthorizationServiceConfiguration.fetchFromIssuer(
                            authServiceConfig.uri,
                            { authConfig: AuthorizationServiceConfiguration?,
                              exception: AuthorizationException? ->

                                if (authConfig != null) {
                                    AuthState(authConfig).andThenStore()
                                    cont.resume(authConfig)
                                } else {
                                    cont.resumeWithException(
                                        exception
                                            ?: RuntimeException("Both config and exception were null")
                                    )
                                }
                            },
                            DefaultConnectionBuilder.INSTANCE
                        )
                    }
            }
        }.also {
            authServiceConfiguration = it
            prepareCustomTabs()
        }
    }

    private suspend fun exchangeCodeForAccessToken(
        authorizationService: AuthorizationService,
        authResponse: AuthorizationResponse
    ): OAuth2State? {
        return suspendCoroutine { cont ->
            authorizationService.performTokenRequest(
                authResponse.createTokenExchangeRequest()
            ) { tokenResponse, authException ->
                authState.andThenStore { update(tokenResponse, authException) }

                if (tokenResponse != null) {
                    cont.resume(authState.toOAuth2State())
                } else {
                    cont.resumeWithException(
                        authException ?: RuntimeException("Both response and exception were null")
                    )
                }
            }
        }
    }

    override fun logout(): Boolean {
        val wasLoggedIn = authState.accessToken != null
        AuthState().andThenStore()
        return wasLoggedIn
    }

    private inline fun AuthState.andThenStore(
        block: (AuthState.() -> Unit) = { /* do nothing */ }
    ) {
        authState = this
        block.invoke(this)
        authStateStorage.writeAuthState(authStateKey, this)
        this@OAuth2Service.authStateFlow.value = toOAuth2State()
    }

    private fun AuthState.toOAuth2State(): OAuth2State {
        return OAuth2State(
            accessToken = accessToken
        )
    }

}
