package me.squeezymo.streamingservices.impl.ui.widget.internal

import android.animation.Animator
import android.animation.ValueAnimator
import android.content.ClipDescription
import android.view.animation.AccelerateInterpolator
import androidx.core.animation.doOnEnd
import androidx.core.animation.doOnStart
import me.squeezymo.core.domain.data.StreamingServiceID
import me.squeezymo.streamingservices.api.domain.ui.getUiName
import me.squeezymo.streamingservices.impl.R
import me.squeezymo.streamingservices.impl.ui.dragndrop.CustomOnDragAndDropListener
import me.squeezymo.streamingservices.impl.ui.widget.StreamingServicePicker
import me.squeezymo.streamingservices.impl.ui.widget.StreamingServiceWidget
import me.squeezymo.streamingservices.impl.ui.widget.StreamingServiceWidgetUiState

internal class StreamingServiceDropListener(
    private val picker: StreamingServicePicker,
    private val widget: StreamingServiceWidget,
    private val clipDescriptionServiceId: (ClipDescription) -> StreamingServiceID,
    private val onTryToMigrateListener: (from: StreamingServiceID, to: StreamingServiceID) -> Unit
) : CustomOnDragAndDropListener() {

    companion object {

        private const val SCALE_ON_DRAG = 1.15F
        private const val ENTER_ANIM_DURATION_MILLIS = 200L
        private const val EXIT_ANIM_DURATION_MILLIS = 50L

    }

    private var runningEnterAnimator: Animator? = null
    private var runningExitAnimator: Animator? = null

    override fun canAcceptDrop(clipDescription: ClipDescription): Boolean {
        return widget.state.isEnabled &&
                clipDescriptionServiceId(clipDescription) != widget.state.service.id
    }

    override fun onEntered(state: Any, clipDescription: ClipDescription) {
        state as StreamingServiceWidget

        when (widget.state.authState) {
            StreamingServiceWidgetUiState.AuthState.Authenticated -> {
                picker.setHintText(
                    picker.resources.getString(
                        R.string.streaming_services_hint_release_to_migrate,
                        state.state.service.getUiName(picker.resources),
                        widget.state.service.getUiName(picker.resources)
                    )
                )
                enterWidget()
            }
            StreamingServiceWidgetUiState.AuthState.Unauthenticated -> {
                picker.setHintText(
                    picker.resources.getString(
                        R.string.streaming_services_hint_auth_to_migrate,
                        widget.state.service.getUiName(picker.resources)
                    ),
                    isWarning = true
                )
                enterWidget()
            }
            StreamingServiceWidgetUiState.AuthState.Unknown -> {
                /* do nothing */
            }
        }
    }

    override fun onExited(state: Any, clipDescription: ClipDescription) {
        picker.setHintText(picker.getDefaultHint())
        exitWidget()
    }

    override fun onDrop(state: Any, clipDescription: ClipDescription) {
        state as StreamingServiceWidget

        if (widget.state.authState is StreamingServiceWidgetUiState.AuthState.Authenticated) {
            onTryToMigrateListener(
                state.state.service.id,
                widget.state.service.id
            )
        }
    }

    override fun onEnded(state: Any) {
        exitWidget()
    }

    private fun enterWidget() {
        ValueAnimator
            .ofFloat(1F, SCALE_ON_DRAG)
            .apply {
                addUpdateListener { valueAnimator ->
                    val scale = valueAnimator.animatedValue as Float

                    widget.scaleX = scale
                    widget.scaleY = scale
                }

                interpolator = AccelerateInterpolator()
                duration = ENTER_ANIM_DURATION_MILLIS

                doOnStart {
                    runningEnterAnimator?.cancel()
                    runningExitAnimator?.cancel()

                    runningEnterAnimator = it
                }

                doOnEnd {
                    runningEnterAnimator = null
                }
            }
            .start()
    }

    private fun exitWidget() {
        if (widget.scaleX == 1F || runningExitAnimator != null) {
            return
        }

        ValueAnimator
            .ofFloat(widget.scaleX, 1F)
            .apply {
                addUpdateListener { valueAnimator ->
                    val scale = valueAnimator.animatedValue as Float

                    widget.scaleX = scale
                    widget.scaleY = scale
                }

                interpolator = AccelerateInterpolator()
                duration = EXIT_ANIM_DURATION_MILLIS

                doOnStart {
                    runningEnterAnimator?.cancel()
                    runningExitAnimator?.cancel()

                    runningExitAnimator = it
                }

                doOnEnd {
                    runningExitAnimator = null
                }
            }
            .start()
    }

}
