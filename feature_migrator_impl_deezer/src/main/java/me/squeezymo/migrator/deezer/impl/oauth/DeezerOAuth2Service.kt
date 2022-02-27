package me.squeezymo.migrator.deezer.impl.oauth

import android.content.Intent
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultLauncher
import androidx.browser.customtabs.CustomTabsIntent
import androidx.browser.customtabs.CustomTabsSession
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import me.squeezymo.core.domain.data.StreamingServiceID
import me.squeezymo.migrator.deezer.impl.oauth.data.DeezerAuthState
import me.squeezymo.migrator.deezer.impl.oauth.storage.AuthStateStorage
import me.squeezymo.oauth.api.IOAuth2Service
import me.squeezymo.oauth.api.OAuth2Config
import me.squeezymo.oauth.api.OAuth2State
import net.openid.appauth.*
import net.openid.appauth.connectivity.DefaultConnectionBuilder
import java.util.concurrent.TimeUnit
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

internal class DeezerOAuth2Service @AssistedInject constructor(
    @Assisted override val serviceId: StreamingServiceID,
    @Assisted private val config: OAuth2Config,
    private val authStateStorage: AuthStateStorage
) : IOAuth2Service {

    init {
        check(config.authResponseType == OAuth2Config.AuthResponseType.TOKEN) {
            "Client-side Deezer flow expects \"response_type=token\". " +
                    "Actual parameter is \"response_type=${config.authResponseType.value}\""
        }
    }

    private val authService = MutableStateFlow<AuthorizationService?>(null)
    private val authStateKey: String = "as_$serviceId"
    private var authState: DeezerAuthState? = authStateStorage.readAuthState(authStateKey)
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
        val currentAuthState = authState ?: return null

        val expiresInMillis = TimeUnit.MILLISECONDS.convert(currentAuthState.expiresInSeconds, TimeUnit.SECONDS)
        val isExpired = System.currentTimeMillis() >= currentAuthState.receivedMillis + expiresInMillis

        if (isExpired) {
            currentAuthState.andThenStore {
                null
            }

            return null
        }

        return currentAuthState.toOAuth2State()
    }

    override suspend fun performAuthorizationRequest(
        activityResultLauncher: ActivityResultLauncher<Intent>,
        customTabsIntent: CustomTabsIntent
    ) {
        activityResultLauncher.launch(
            waitForAuthService()
                .getAuthorizationRequestIntent(
                    AuthorizationRequest
                        .Builder(
                            fetchAuthorizationServiceConfiguration(),
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
                        .build(),
                    customTabsIntent
                )
        )
    }

    override suspend fun handleAuthorizationResult(intent: Intent): OAuth2State? {
        // Redirect example:
        // https://filch-a0eb0.firebaseapp.com/oauth2redirect#access_token=frb3250ojWOFZki7CEQLd4PN50M9cM9hbPcuUNyp1FXO9ppth6S&expires=2719

        val fragment = intent.data?.fragment

        if (fragment == null) {
            authState.andThenStore {
                null
            }
        }
        else {
            val receivedMillis = System.currentTimeMillis()
            var accessTokenTmp: String? = null
            var expiresInSecondsTmp: Long? = null

            fragment
                .split('&')
                .forEach { keyValue ->
                    keyValue
                        .split('=')
                        .takeIf { it.size == 2 }
                        ?.let { (key, value) ->
                            when (key) {
                                "access_token" -> accessTokenTmp = value
                                "expires" -> expiresInSecondsTmp = value.toLong()
                            }
                        }
                }

            authState.andThenStore {
                val accessToken = accessTokenTmp
                val expiresInSeconds = expiresInSecondsTmp

                if (accessToken == null) {
                    null
                }
                else {
                    DeezerAuthState(
                        accessToken = accessToken,
                        receivedMillis = receivedMillis,
                        expiresInSeconds = expiresInSeconds ?: 0L
                    )
                }
            }
        }

        return authState.toOAuth2State()
    }

    override fun logout(): Boolean {
        val wasLoggedIn = authState?.accessToken != null
        null.andThenStore()
        return wasLoggedIn
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
                suspendCoroutine { cont ->
                    AuthorizationServiceConfiguration.fetchFromIssuer(
                        authServiceConfig.uri,
                        { authConfig: AuthorizationServiceConfiguration?,
                          exception: AuthorizationException? ->

                            if (authConfig != null) {
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

    private inline fun DeezerAuthState?.andThenStore(
        block: (DeezerAuthState?.() -> DeezerAuthState?) = { this }
    ) {
        val newAuthState = block.invoke(this)

        authState = newAuthState
        authStateStorage.writeAuthState(authStateKey, newAuthState)
        this@DeezerOAuth2Service.authStateFlow.value = newAuthState.toOAuth2State()
    }

    private fun DeezerAuthState?.toOAuth2State(): OAuth2State {
        return OAuth2State(
            accessToken = this?.accessToken
        )
    }

}
