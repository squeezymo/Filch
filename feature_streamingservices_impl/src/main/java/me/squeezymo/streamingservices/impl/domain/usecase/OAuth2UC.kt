package me.squeezymo.streamingservices.impl.domain.usecase

import android.content.Context
import android.content.Intent
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultLauncher
import androidx.browser.customtabs.CustomTabsIntent
import androidx.browser.customtabs.CustomTabsSession
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import me.squeezymo.core.ext.clear
import me.squeezymo.core.ext.editAndApply
import me.squeezymo.oauth.api.IOAuth2Broker
import me.squeezymo.oauth.api.OAuth2State
import me.squeezymo.streamingservices.api.domain.data.StreamingService
import me.squeezymo.streamingservices.api.domain.usecase.IOAuth2UC
import javax.inject.Inject

internal class OAuth2UC @Inject constructor(
    @ApplicationContext appContext: Context,
    private val oAuth2Broker: IOAuth2Broker
) : IOAuth2UC {

    companion object {

        private const val SPNAME_OAUTH2 = "oauth2_progress"
        private const val SPKEY_SERVICE_IN_PROGRESS = "service_in_progress"

    }

    private val prefs = appContext.getSharedPreferences(SPNAME_OAUTH2, Context.MODE_PRIVATE)

    override fun bindOAuthServices(
        activity: ComponentActivity,
        services: List<StreamingService>?
    ) {
        oAuth2Broker
            .getAllOAuth2Services(services?.map(StreamingService::id))
            .forEach { oauth2Service ->
                oauth2Service.bind(activity)
            }
    }

    override fun notifyOnCustomTabsSession(
        session: CustomTabsSession,
        services: List<StreamingService>?
    ) {
        oAuth2Broker
            .getAllOAuth2Services(services?.map(StreamingService::id))
            .forEach { oauth2Service ->
                oauth2Service.notifyOnCustomTabsSession(session)
            }
    }

    override suspend fun updateOAuthAccessTokens(
        services: List<StreamingService>?
    ) {
        oAuth2Broker
            .getAllOAuth2Services(services?.map(StreamingService::id))
            .forEach { oauth2Service ->
                oauth2Service.getFreshAccessState()
            }
    }

    override fun createOAuthAccessStateFlow(
        services: List<StreamingService>?
    ): Flow<Map<StreamingService, OAuth2State>> {
        return combine(
            oAuth2Broker
                .getAllOAuth2Services(services?.map(StreamingService::id))
                .map { oauth2Service ->
                    oauth2Service
                        .authStateFlow
                        .map { authState ->
                            StreamingService.requireById(oauth2Service.serviceId) to authState
                        }
                }
        ) { result: Array<Pair<StreamingService, OAuth2State>> ->

            result.associate { (streamingService, authState) ->
                streamingService to authState
            }
        }
    }

    override suspend fun performOAuthRequest(
        streamingService: StreamingService,
        customTabsIntent: CustomTabsIntent,
        activityResultLauncher: ActivityResultLauncher<Intent>
    ) {
        val oAuth2Service = requireNotNull(oAuth2Broker.getOAuth2ServiceById(streamingService.id))

        prefs.editAndApply {
            putString(SPKEY_SERVICE_IN_PROGRESS, streamingService.id)
        }

        oAuth2Service.performAuthorizationRequest(activityResultLauncher, customTabsIntent)
    }

    override suspend fun handleOAuthResult(intent: Intent): OAuth2State? {
        val streamingService = prefs
            .getString(SPKEY_SERVICE_IN_PROGRESS, null)
            ?.let(StreamingService::requireById) ?: return null

        prefs.clear()

        val oAuth2Service = requireNotNull(oAuth2Broker.getOAuth2ServiceById(streamingService.id))
        return oAuth2Service.handleAuthorizationResult(intent)
    }

    override fun logout(service: StreamingService): Boolean {
        return oAuth2Broker.getOAuth2ServiceById(service.id)?.logout() ?: false
    }

}
