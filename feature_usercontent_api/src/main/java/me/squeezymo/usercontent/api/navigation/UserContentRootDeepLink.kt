package me.squeezymo.usercontent.api.navigation

import android.net.Uri
import androidx.core.net.toUri
import me.squeezymo.core.domain.data.StreamingServiceID

object UserContentRootDeepLink {

    fun create(
        fromServiceId: StreamingServiceID,
        toServiceId: StreamingServiceID,
        autoMigrate: Boolean
    ): Uri {
        return ("filch-a0eb0.firebaseapp.com://user_content?" +
                "from=${fromServiceId}&" +
                "to=${toServiceId}&" +
                "auto_migrate=${autoMigrate}").toUri()
    }

}
