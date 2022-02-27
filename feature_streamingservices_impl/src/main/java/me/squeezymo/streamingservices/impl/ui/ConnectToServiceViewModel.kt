package me.squeezymo.streamingservices.impl.ui

import android.content.Intent
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultLauncher
import androidx.browser.customtabs.CustomTabsIntent
import androidx.browser.customtabs.CustomTabsSession
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import me.squeezymo.core.ext.collectTo
import me.squeezymo.core.ext.toBundle
import me.squeezymo.core.ui.BaseViewModel
import me.squeezymo.core.ui.IBaseViewModel
import me.squeezymo.oauth.api.AccessToken
import me.squeezymo.oauth.api.OAuth2State
import me.squeezymo.streamingservices.api.domain.data.StreamingService
import me.squeezymo.streamingservices.api.domain.navigation.StreamingServicePickerDeepLink
import me.squeezymo.streamingservices.api.domain.usecase.IOAuth2UC
import me.squeezymo.streamingservices.impl.ui.uistate.ConnectToServiceUiState
import javax.inject.Inject

internal interface IConnectToServiceViewModel : IBaseViewModel {

    val toService: StreamingService

    val uiState: StateFlow<ConnectToServiceUiState>

    fun bindOAuthService(
        activity: ComponentActivity
    )

    fun notifyOnCustomTabsSession(
        session: CustomTabsSession
    )

    fun connect(
        activityResultLauncher: ActivityResultLauncher<Intent>,
        customTabsIntent: CustomTabsIntent
    )

    fun handleOAuthResult(
        intent: Intent
    )

    fun openServicePicker()

}

@HiltViewModel
internal class ConnectToServiceViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val oAuth2UC: IOAuth2UC
) : BaseViewModel(savedStateHandle), IConnectToServiceViewModel {

    private val args = ConnectToServiceFragmentArgs.fromBundle(savedStateHandle.toBundle())

    override val toService = StreamingService.requireById(args.to)

    private val accessState: MutableStateFlow<OAuth2State> =
        MutableStateFlow(OAuth2State(args.accessToken))

    override val uiState: MutableStateFlow<ConnectToServiceUiState> =
        MutableStateFlow(createUiState(accessState.value.accessToken))

    init {
        viewModelScope.launch {
            launch(Dispatchers.Default) {
                oAuth2UC
                    .createOAuthAccessStateFlow()
                    .map { accessStateByService ->
                        requireNotNull(accessStateByService[toService])
                    }
                    .collectTo(accessState)
            }

            launch {
                accessState
                    .map { oAuth2State ->
                        createUiState(oAuth2State.accessToken)
                    }
                    .collectTo(uiState)
            }
        }
    }

    private fun createUiState(
        accessToken: AccessToken?
    ): ConnectToServiceUiState {
        return ConnectToServiceUiState(
            service = toService,
            isConnected = accessToken != null
        )
    }

    override fun bindOAuthService(
        activity: ComponentActivity
    ) {
        oAuth2UC.bindOAuthServices(activity)
    }

    override fun notifyOnCustomTabsSession(
        session: CustomTabsSession
    ) {
        oAuth2UC.notifyOnCustomTabsSession(session)
    }

    override fun connect(
        activityResultLauncher: ActivityResultLauncher<Intent>,
        customTabsIntent: CustomTabsIntent
    ) {
        viewModelScope.launch(Dispatchers.Default) {
            oAuth2UC.performOAuthRequest(
                toService,
                customTabsIntent,
                activityResultLauncher
            )
        }
    }

    override fun handleOAuthResult(
        intent: Intent
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            oAuth2UC.handleOAuthResult(intent)
        }
    }

    override fun openServicePicker() {
        navigateTo(StreamingServicePickerDeepLink.create(toService.id))
    }

}
