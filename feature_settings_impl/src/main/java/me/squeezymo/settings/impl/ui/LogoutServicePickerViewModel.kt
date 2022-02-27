package me.squeezymo.settings.impl.ui

import androidx.activity.ComponentActivity
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import me.squeezymo.core.domain.data.StreamingServiceID
import me.squeezymo.core.ext.collectTo
import me.squeezymo.core.ui.BaseViewModel
import me.squeezymo.core.ui.IBaseViewModel
import me.squeezymo.oauth.api.OAuth2State
import me.squeezymo.settings.impl.ui.event.LogoutResultEvent
import me.squeezymo.settings.impl.ui.widget.LogoutStreamingServicePickerUiState
import me.squeezymo.settings.impl.ui.widget.StreamingServiceWidgetUiState
import me.squeezymo.streamingservices.api.domain.data.StreamingService
import me.squeezymo.streamingservices.api.domain.usecase.IOAuth2UC
import javax.inject.Inject

internal interface ILogoutStreamingServicePickerViewModel : IBaseViewModel {

    val uiState: StateFlow<LogoutStreamingServicePickerUiState?>

    val logoutResultEvent: Flow<LogoutResultEvent?>

    fun notifyOnLogoutResultHandled()

    fun bindOAuthService(
        activity: ComponentActivity
    )

    fun pickService(
        id: StreamingServiceID
    )

}

@HiltViewModel
internal class LogoutStreamingServicePickerViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val oAuth2UC: IOAuth2UC
) : BaseViewModel(savedStateHandle), ILogoutStreamingServicePickerViewModel {

    private val accessStateByService = MutableStateFlow(emptyMap<StreamingService, OAuth2State>())

    override val uiState: MutableStateFlow<LogoutStreamingServicePickerUiState?> =
        MutableStateFlow(null)

    override val logoutResultEvent: MutableStateFlow<LogoutResultEvent?> =
        MutableStateFlow(null)

    init {
        viewModelScope.launch {
            launch {
                oAuth2UC.updateOAuthAccessTokens()
            }

            launch(Dispatchers.Default) {
                oAuth2UC
                    .createOAuthAccessStateFlow()
                    .collectTo(accessStateByService)
            }

            launch {
                accessStateByService
                    .map(::createUiState)
                    .collectTo(uiState)
            }
        }
    }

    override fun bindOAuthService(
        activity: ComponentActivity
    ) {
        oAuth2UC.bindOAuthServices(activity)
    }

    override fun pickService(id: StreamingServiceID) {
        val streamingService = StreamingService.requireById(id)

        logoutResultEvent.value = LogoutResultEvent(
            streamingService = streamingService,
            isSuccess = oAuth2UC.logout(streamingService)
        )
    }

    override fun notifyOnLogoutResultHandled() {
        logoutResultEvent.value = null
    }

    private fun createUiState(
        accessStateByService: Map<StreamingService, OAuth2State>
    ): LogoutStreamingServicePickerUiState {
        return LogoutStreamingServicePickerUiState(
            serviceStates = StreamingService.values()
                .mapNotNull { streamingService ->
                    val accessState = accessStateByService[streamingService]

                    if (accessState?.accessToken == null) {
                        null
                    }
                    else {
                        StreamingServiceWidgetUiState(
                            service = streamingService,
                            isEnabled = streamingService.isEnabled,
                            authState = StreamingServiceWidgetUiState.AuthState.Authenticated
                        )
                    }
                }
        )
    }

}
