package me.squeezymo.streamingservices.api.domain.navigation

import android.net.Uri
import androidx.core.net.toUri

object StreamingServicesDeepLink {

    fun create(): Uri {
        return "filch-a0eb0.firebaseapp.com://streaming_services".toUri()
    }

}
