package me.squeezymo.streamingservices.impl.ui.widget.internal

import android.content.ClipDescription
import androidx.core.view.isInvisible
import me.squeezymo.core.domain.data.StreamingServiceID
import me.squeezymo.streamingservices.impl.ui.dragndrop.CustomOnDragAndDropListener
import me.squeezymo.streamingservices.impl.ui.widget.StreamingServicePicker
import me.squeezymo.streamingservices.impl.ui.widget.StreamingServiceWidget

internal class StreamingServiceDragListener(
    private val picker: StreamingServicePicker,
    private val widget: StreamingServiceWidget,
    private val clipDescriptionServiceId: (ClipDescription) -> StreamingServiceID,
    private val onTryToMigrateListener: (from: StreamingServiceID, to: StreamingServiceID) -> Unit
) : CustomOnDragAndDropListener() {

    override fun canAcceptDrop(clipDescription: ClipDescription): Boolean {
        return true
    }

    override fun onStarted(state: Any) {
        state as StreamingServiceWidget
        state.isInvisible = true
    }

    override fun onEnded(state: Any) {
        state as StreamingServiceWidget

        state.showCaption(true)
        state.isInvisible = false

        picker.setHintText(picker.getDefaultHint())

        widget.setOnDragListener(
            StreamingServiceDropListener(
                picker,
                widget,
                clipDescriptionServiceId,
                onTryToMigrateListener
            )
        )
    }

}
