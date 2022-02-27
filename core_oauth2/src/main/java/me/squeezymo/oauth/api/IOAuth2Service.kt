package me.squeezymo.oauth.api

import android.content.Intent
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultLauncher
import androidx.browser.customtabs.CustomTabsIntent
import androidx.browser.customtabs.CustomTabsSession
import kotlinx.coroutines.flow.StateFlow

interface IOAuth2Service : IOAuth2StateProvider {

    val authStateFlow: StateFlow<OAuth2State>

    fun bind(
        activity: ComponentActivity
    )

    fun notifyOnCustomTabsSession(
        session: CustomTabsSession
    )

    suspend fun performAuthorizationRequest(
        activityResultLauncher: ActivityResultLauncher<Intent>,
        customTabsIntent: CustomTabsIntent
    )

    suspend fun handleAuthorizationResult(
        intent: Intent
    ): OAuth2State?

    fun logout(): Boolean

}
