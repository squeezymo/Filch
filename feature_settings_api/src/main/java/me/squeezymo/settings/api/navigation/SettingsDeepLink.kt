package me.squeezymo.settings.api.navigation

import android.net.Uri
import androidx.core.net.toUri

object SettingsDeepLink {

    fun create(): Uri {
        return "filch-a0eb0.firebaseapp.com://settings".toUri()
    }

}
