package me.squeezymo.streamingservices.impl.ui.widget

import android.content.Context
import android.util.AttributeSet
import me.squeezymo.core.ext.getColorCompat
import me.squeezymo.core.uicomponents.widget.BaseStreamingServiceWidget
import me.squeezymo.streamingservices.api.domain.data.StreamingService
import me.squeezymo.streamingservices.api.domain.ui.getIconResId
import me.squeezymo.streamingservices.api.domain.ui.getUiName
import me.squeezymo.streamingservices.impl.R

internal data class StreamingServiceWidgetUiState(
    val service: StreamingService,
    val isEnabled: Boolean,
    val authState: AuthState
) {

    sealed class AuthState {
        object Unknown : AuthState()
        object Authenticated : AuthState()
        object Unauthenticated : AuthState()
    }

}

internal class StreamingServiceWidget @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : BaseStreamingServiceWidget(context, attrs, defStyleAttr) {

    lateinit var state: StreamingServiceWidgetUiState
        private set

    init {
        if (isInEditMode) {
            setState(
                StreamingServiceWidgetUiState(
                    service = StreamingService.DEEZER,
                    isEnabled = true,
                    authState = StreamingServiceWidgetUiState.AuthState.Authenticated
                )
            )
        }
    }

    fun setState(state: StreamingServiceWidgetUiState) {
        this.state = state

        setServiceInfo(
            serviceUiName = state.service.getUiName(resources),
            isServiceEnabled = state.isEnabled,
            serviceIconResId = state.service.getIconResId(),
            serviceIconColor = when (state.authState) {
                StreamingServiceWidgetUiState.AuthState.Authenticated -> {
                    context.getColorCompat(R.color.service_icon_authenticated)
                }
                StreamingServiceWidgetUiState.AuthState.Unauthenticated -> {
                    context.getColorCompat(R.color.service_icon_unauthenticated)
                }
                StreamingServiceWidgetUiState.AuthState.Unknown -> {
                    context.getColorCompat(R.color.service_icon_unauthenticated)
                }
            },
            backgroundResId = when (state.authState) {
                StreamingServiceWidgetUiState.AuthState.Authenticated -> {
                    R.drawable.bg_streaming_service_authenticated
                }
                StreamingServiceWidgetUiState.AuthState.Unauthenticated -> {
                    R.drawable.bg_streaming_service_unauthenticated
                }
                StreamingServiceWidgetUiState.AuthState.Unknown -> {
                    R.drawable.bg_streaming_service_unauthenticated
                }
            }
        )
    }

}
