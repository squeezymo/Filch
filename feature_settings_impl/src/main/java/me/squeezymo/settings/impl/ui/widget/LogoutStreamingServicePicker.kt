package me.squeezymo.settings.impl.ui.widget

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import com.google.android.flexbox.FlexboxLayout
import me.squeezymo.core.domain.data.StreamingServiceID
import me.squeezymo.settings.impl.R
import me.squeezymo.settings.impl.databinding.VLogoutStreamingServicePickerBinding

internal data class LogoutStreamingServicePickerUiState(
    val serviceStates: List<StreamingServiceWidgetUiState>
)

internal class LogoutStreamingServicePicker @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    companion object {

        private const val MAX_ICONS_PER_ROW = 3

    }

    private val binding = VLogoutStreamingServicePickerBinding.inflate(
        LayoutInflater.from(context),
        this
    )

    init {
        orientation = VERTICAL
    }

    lateinit var onServiceClickListener: (id: StreamingServiceID) -> Unit

    fun setState(state: LogoutStreamingServicePickerUiState) {
        binding.servicesContainer.removeAllViews()

        if (state.serviceStates.isEmpty()) {
            binding.captionTv.text =
                resources.getString(R.string.settings_caption_no_services_to_logout_from)
        }
        else {
            binding.captionTv.text =
                resources.getString(R.string.settings_caption_logout)

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

}
