package me.squeezymo.settings.api.navigation

import android.net.Uri
import androidx.core.net.toUri

object LogoutServicePickerDeepLink {

    fun create(): Uri {
        return "filch-a0eb0.firebaseapp.com://logout".toUri()
    }

}
