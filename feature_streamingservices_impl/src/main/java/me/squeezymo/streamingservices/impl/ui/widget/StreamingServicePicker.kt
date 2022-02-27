package me.squeezymo.streamingservices.impl.ui.widget

import android.annotation.SuppressLint
import android.content.Context
import android.text.style.RelativeSizeSpan
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import com.google.android.flexbox.FlexboxLayout
import me.squeezymo.core.domain.data.StreamingServiceID
import me.squeezymo.core.ext.dp
import me.squeezymo.core.ext.getColorCompat
import me.squeezymo.core.ui.util.TextHighlighter
import me.squeezymo.streamingservices.api.domain.data.StreamingService
import me.squeezymo.streamingservices.impl.R
import me.squeezymo.streamingservices.impl.databinding.VStreamingServicePickerBinding
import me.squeezymo.streamingservices.impl.ui.widget.internal.StreamingServiceDropListener
import me.squeezymo.streamingservices.impl.ui.widget.internal.StreamingServiceTouchListener

internal data class StreamingServicePickerUiState(
    val serviceStates: List<StreamingServiceWidgetUiState>
)

internal class StreamingServicePicker @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    companion object {

        private const val MAX_ICONS_PER_ROW = 3

    }

    private val binding = VStreamingServicePickerBinding.inflate(
        LayoutInflater.from(context),
        this
    )

    private var state: StreamingServicePickerUiState? = null

    lateinit var onServiceClickListener: (id: StreamingServiceID) -> Unit
    lateinit var onTryToMigrateListener: (from: StreamingServiceID, to: StreamingServiceID) -> Unit

    init {
        orientation = VERTICAL

        setPadding(
            0,
            0,
            0,
            dp(64)
        )
        setBackgroundResource(R.drawable.bg_streaming_service_picker)
        clipToOutline = true

        if (isInEditMode) {
            setState(
                StreamingServicePickerUiState(
                    listOf(
                        StreamingServiceWidgetUiState(
                            service = StreamingService.SPOTIFY,
                            isEnabled = true,
                            authState = StreamingServiceWidgetUiState.AuthState.Authenticated
                        ),
                        StreamingServiceWidgetUiState(
                            service = StreamingService.DEEZER,
                            isEnabled = true,
                            authState = StreamingServiceWidgetUiState.AuthState.Authenticated
                        )
                    )
                )
            )
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    fun setState(state: StreamingServicePickerUiState) {
        this.state = state

        binding.servicesContainer.removeAllViews()

        state.serviceStates.forEachIndexed { index, serviceState ->
            binding.servicesContainer.addView(
                StreamingServiceWidget(context).apply {
                    id = View.generateViewId()
                    setState(serviceState)

                    if (serviceState.isEnabled) {
                        setOnDragListener(
                            StreamingServiceDropListener(
                                this@StreamingServicePicker,
                                this,
                                { it.label.toString() },
                                onTryToMigrateListener
                            )
                        )
                        setOnTouchListener(
                            StreamingServiceTouchListener(
                                this@StreamingServicePicker,
                                this,
                                { it.label.toString() },
                                onTryToMigrateListener,
                                onServiceClickListener
                            )
                        )
                    }
                },
                FlexboxLayout.LayoutParams(
                    resources.getDimensionPixelSize(R.dimen.service_widget_width),
                    ViewGroup.LayoutParams.WRAP_CONTENT
                ).apply {
                    isWrapBefore = index % MAX_ICONS_PER_ROW == 0
                }
            )
        }

        setHintText(getDefaultHint())
    }

    internal fun setHintText(text: String, isWarning: Boolean = false) {
        binding.hintTv.text = TextHighlighter(Regex("<b>(.*?)</b>"))
            .createHighlightableText(
                text,
                highlightSpan = {
                    RelativeSizeSpan(1.3F)
                }
            )
        binding.hintTv.setTextColor(
            context.getColorCompat(
                if (isWarning) {
                    R.color.warning
                } else {
                    R.color.text_secondary
                }
            )
        )
    }

    internal fun getDefaultHint(): String {
        val hasAtLeastTwoAuthorizedServices =
            state
                ?.serviceStates
                ?.count {
                    it.authState is StreamingServiceWidgetUiState.AuthState.Authenticated
                }
                ?.let { it >= 2 } ?: false

        return if (hasAtLeastTwoAuthorizedServices) {
            resources.getString(R.string.streaming_services_hint_use_dragging)
        } else {
            resources.getString(R.string.streaming_services_hint_pick_src_service)
        }
    }

}
