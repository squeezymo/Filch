package me.squeezymo.streamingservices.impl.ui.widget.internal

import android.content.ClipData
import android.content.ClipDescription
import android.os.Build
import android.view.MotionEvent
import android.view.View
import android.widget.LinearLayout
import androidx.annotation.Px
import me.squeezymo.core.domain.data.StreamingServiceID
import me.squeezymo.core.ext.dp
import me.squeezymo.core.ext.startDragAndDropCompat
import me.squeezymo.streamingservices.impl.ui.widget.StreamingServicePicker
import me.squeezymo.streamingservices.impl.ui.widget.StreamingServiceWidget
import me.squeezymo.streamingservices.impl.ui.widget.StreamingServiceWidgetUiState
import kotlin.math.abs

internal class StreamingServiceTouchListener(
    private val picker: StreamingServicePicker,
    private val widget: StreamingServiceWidget,
    private val clipDescriptionServiceId: (ClipDescription) -> StreamingServiceID,
    private val onTryToMigrateListener: (from: StreamingServiceID, to: StreamingServiceID) -> Unit,
    private val onServiceClickListener: (id: StreamingServiceID) -> Unit
) : View.OnTouchListener {

    companion object {

        private const val MAX_ICONS_PER_ROW = 3
        private const val SCALE_ON_DRAG = 1.15F
        private const val ENTER_ANIM_DURATION_MILLIS = 200L
        private const val EXIT_ANIM_DURATION_MILLIS = 50L
        private const val CLICK_THRESHOLD_MILLIS = 300L
        private const val MOVE_THRESHOLD_DP = 4

    }

    @Px
    private val moveThresholdPx = widget.dp(MOVE_THRESHOLD_DP)

    private var actionDownMillis: Long = 0
    private var actionDownX: Float = 0F
    private var actionDownY: Float = 0F
    private var hasStartedDragging: Boolean = false

    override fun onTouch(view: View, event: MotionEvent): Boolean {
        return when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                actionDownMillis = System.currentTimeMillis()
                actionDownX = event.x
                actionDownY = event.y
                hasStartedDragging = false

                true
            }
            MotionEvent.ACTION_MOVE -> {
                if (widget.state.authState !is StreamingServiceWidgetUiState.AuthState.Authenticated) {
                    true
                } else if (abs(actionDownX - event.x) >= moveThresholdPx ||
                    abs(actionDownY - event.y) >= moveThresholdPx
                ) {
                    val data = ClipData.newPlainText(widget.state.service.id, "")

                    widget.setOnDragListener(
                        StreamingServiceDragListener(
                            picker,
                            widget,
                            { it.label.toString() },
                            onTryToMigrateListener
                        )
                    )
                    widget.showCaption(false)
                    widget.startDragAndDropCompat(
                        data,
                        View.DragShadowBuilder(widget),
                        flags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                            LinearLayout.DRAG_FLAG_OPAQUE
                        } else {
                            0
                        }
                    )

                    hasStartedDragging = true
                    false
                } else {
                    true
                }
            }
            MotionEvent.ACTION_UP -> {
                val currentTimeMillis = System.currentTimeMillis()
                if (!hasStartedDragging && currentTimeMillis - actionDownMillis <= CLICK_THRESHOLD_MILLIS) {
                    onServiceClickListener(widget.state.service.id)
                }

                true
            }
            else -> {
                false
            }
        }
    }
}
