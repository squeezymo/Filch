package me.squeezymo.streamingservices.impl.ui.widget

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import com.google.android.flexbox.FlexboxLayout
import me.squeezymo.core.domain.data.StreamingServiceID
import me.squeezymo.streamingservices.impl.R
import me.squeezymo.streamingservices.impl.databinding.VSrcStreamingServicePickerBinding

internal data class SrcStreamingServicePickerUiState(
    val serviceStates: List<StreamingServiceWidgetUiState>
)

internal class SrcStreamingServicePicker @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    companion object {

        private const val MAX_ICONS_PER_ROW = 3

    }

    private val binding = VSrcStreamingServicePickerBinding.inflate(
        LayoutInflater.from(context),
        this
    )

    init {
        orientation = VERTICAL
    }

    lateinit var onServiceClickListener: (id: StreamingServiceID) -> Unit

    fun setState(state: SrcStreamingServicePickerUiState) {
        binding.servicesContainer.removeAllViews()

        state.serviceStates.forEachIndexed { index, serviceState ->
            binding.servicesContainer.addView(
                StreamingServiceWidget(context).apply {
                    id = View.generateViewId()
                    setState(serviceState)
                    setOnClickListener {
                        onServiceClickListener(serviceState.service.id)
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
    }

}
