package me.squeezymo.streamingservices.api.domain.navigation

import android.net.Uri
import androidx.core.net.toUri
import me.squeezymo.core.domain.data.StreamingServiceID

object StreamingServicePickerDeepLink {

    fun create(
        targetService: StreamingServiceID
    ): Uri {
        return "filch-a0eb0.firebaseapp.com://streaming_service_picker?target_service=${targetService}".toUri()
    }

}
