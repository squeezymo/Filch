package me.squeezymo.streamingservices.api.domain.navigation

import android.net.Uri
import androidx.core.net.toUri
import me.squeezymo.core.domain.data.StreamingServiceID
import me.squeezymo.oauth.api.AccessToken

object ConnectToServiceDeepLink {

    fun create(
        toServiceId: StreamingServiceID,
        accessToken: AccessToken?
    ): Uri {
        return "filch-a0eb0.firebaseapp.com://connect?to=${toServiceId}"
            .toUri()
            .buildUpon()
            .let {
                if (accessToken == null) {
                    it.appendQueryParameter("access_token", accessToken)
                }
                else {
                    it
                }
            }
            .build()
    }

}
