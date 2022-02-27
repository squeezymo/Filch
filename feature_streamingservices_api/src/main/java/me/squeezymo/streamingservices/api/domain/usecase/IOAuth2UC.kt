package me.squeezymo.streamingservices.api.domain.usecase

import android.content.Intent
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultLauncher
import androidx.browser.customtabs.CustomTabsIntent
import androidx.browser.customtabs.CustomTabsSession
import kotlinx.coroutines.flow.Flow
import me.squeezymo.oauth.api.OAuth2State
import me.squeezymo.streamingservices.api.domain.data.StreamingService

interface IOAuth2UC {

    fun bindOAuthServices(
        activity: ComponentActivity,
        services: List<StreamingService>? = null
    )

    fun notifyOnCustomTabsSession(
        session: CustomTabsSession,
        services: List<StreamingService>? = null
    )

    suspend fun updateOAuthAccessTokens(
        services: List<StreamingService>? = null
    )

    fun createOAuthAccessStateFlow(
        services: List<StreamingService>? = null
    ): Flow<Map<StreamingService, OAuth2State>>

    suspend fun performOAuthRequest(
        streamingService: StreamingService,
        customTabsIntent: CustomTabsIntent,
        activityResultLauncher: ActivityResultLauncher<Intent>
    )

    suspend fun handleOAuthResult(
        intent: Intent
    ) : OAuth2State?

    fun logout(
        service: StreamingService
    ): Boolean

}
