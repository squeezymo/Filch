package me.squeezymo.streamingservices.impl.ui

import androidx.activity.ComponentActivity
import androidx.browser.customtabs.CustomTabsSession
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import me.squeezymo.core.domain.data.StreamingServiceID
import me.squeezymo.core.ext.collectTo
import me.squeezymo.core.ui.BaseViewModel
import me.squeezymo.core.ui.IBaseViewModel
import me.squeezymo.oauth.api.OAuth2State
import me.squeezymo.settings.api.navigation.SettingsDeepLink
import me.squeezymo.streamingservices.api.domain.data.StreamingService
import me.squeezymo.streamingservices.api.domain.navigation.ConnectToServiceDeepLink
import me.squeezymo.streamingservices.api.domain.usecase.IOAuth2UC
import me.squeezymo.streamingservices.impl.ui.data.MigrationOption
import me.squeezymo.streamingservices.impl.ui.widget.StreamingServicePickerUiState
import me.squeezymo.streamingservices.impl.ui.widget.StreamingServiceWidgetUiState
import me.squeezymo.usercontent.api.navigation.UserContentRootDeepLink
import javax.inject.Inject

internal interface IStreamingServicesViewModel : IBaseViewModel {

    val pickerUiState: StateFlow<StreamingServicePickerUiState>

    fun bindOAuthServices(
        activity: ComponentActivity
    )

    fun notifyOnCustomTabsSession(
        session: CustomTabsSession
    )

    fun updateOAuthTokens()

    fun navigateToService(
        serviceId: StreamingServiceID
    )

    fun tryToMigrate(
        fromServiceId: StreamingServiceID,
        toServiceId: StreamingServiceID,
        migrationOption: MigrationOption
    )

    fun goToSettings()

}

@HiltViewModel
internal class StreamingServicesViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val oAuth2UC: IOAuth2UC
) : BaseViewModel(savedStateHandle), IStreamingServicesViewModel {

    private val accessStateByService = MutableStateFlow(emptyMap<StreamingService, OAuth2State>())
    override val pickerUiState = MutableStateFlow(createPickerUiState(accessStateByService.value))

    init {
        viewModelScope.launch {
            launch(Dispatchers.Default) {
                oAuth2UC
                    .createOAuthAccessStateFlow()
                    .collectTo(accessStateByService)
            }

            launch(Dispatchers.Default) {
                accessStateByService
                    .map(::createPickerUiState)
                    .collectTo(pickerUiState)
            }
        }
    }

    override fun bindOAuthServices(
        activity: ComponentActivity
    ) {
        oAuth2UC.bindOAuthServices(activity)
    }

    override fun notifyOnCustomTabsSession(
        session: CustomTabsSession
    ) {
        oAuth2UC.notifyOnCustomTabsSession(session)
    }

    override fun updateOAuthTokens() {
        viewModelScope.launch(Dispatchers.IO) {
            oAuth2UC.updateOAuthAccessTokens()
        }
    }

    override fun navigateToService(serviceId: StreamingServiceID) {
        navigateTo(
            ConnectToServiceDeepLink.create(
                serviceId,
                accessStateByService.value[StreamingService.requireById(serviceId)]?.accessToken
            )
        )
    }

    override fun tryToMigrate(
        fromServiceId: StreamingServiceID,
        toServiceId: StreamingServiceID,
        migrationOption: MigrationOption
    ) {
        val serviceFrom = StreamingService.requireById(fromServiceId)
        val serviceTo = StreamingService.requireById(toServiceId)

        if (isAuthorized(serviceFrom) && isAuthorized(serviceTo)) {
            navigateTo(
                UserContentRootDeepLink.create(
                    fromServiceId,
                    toServiceId,
                    migrationOption is MigrationOption.All
                )
            )
        } else {
            // TODO
        }
    }

    private fun createPickerUiState(
        accessStateByService: Map<StreamingService, OAuth2State>
    ): StreamingServicePickerUiState {
        return StreamingServicePickerUiState(
            serviceStates = StreamingService.values().map { streamingService ->
                val accessState = accessStateByService[streamingService]

                StreamingServiceWidgetUiState(
                    service = streamingService,
                    isEnabled = streamingService.isEnabled,
                    authState = when {
                        accessState == null -> {
                            StreamingServiceWidgetUiState.AuthState.Unknown
                        }
                        accessState.accessToken == null -> {
                            StreamingServiceWidgetUiState.AuthState.Unauthenticated
                        }
                        else -> {
                            StreamingServiceWidgetUiState.AuthState.Authenticated
                        }
                    }
                )
            }
        )
    }

    private fun isAuthorized(streamingService: StreamingService): Boolean {
        return accessStateByService.value[streamingService]?.accessToken != null
    }

    override fun goToSettings() {
        navigateTo(SettingsDeepLink.create())
    }

}
