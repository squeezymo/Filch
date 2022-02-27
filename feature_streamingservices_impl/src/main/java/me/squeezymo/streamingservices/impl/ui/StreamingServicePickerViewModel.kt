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
import me.squeezymo.core.domain.data.StreamingServiceID
import me.squeezymo.core.ext.collectTo
import me.squeezymo.core.ext.toBundle
import me.squeezymo.core.ui.BaseViewModel
import me.squeezymo.core.ui.IBaseViewModel
import me.squeezymo.oauth.api.OAuth2State
import me.squeezymo.streamingservices.api.domain.data.StreamingService
import me.squeezymo.streamingservices.api.domain.usecase.IOAuth2UC
import me.squeezymo.streamingservices.impl.R
import me.squeezymo.streamingservices.impl.ui.data.MigrationOption
import me.squeezymo.streamingservices.impl.ui.widget.SrcStreamingServicePickerUiState
import me.squeezymo.streamingservices.impl.ui.widget.StreamingServiceWidgetUiState
import me.squeezymo.usercontent.api.navigation.UserContentRootDeepLink
import javax.inject.Inject

internal interface IStreamingServicePickerViewModel : IBaseViewModel {

    val uiState: StateFlow<SrcStreamingServicePickerUiState?>

    val showMigrationOptionsDialog: StateFlow<StreamingServiceID?>

    fun bindOAuthService(
        activity: ComponentActivity
    )

    fun notifyOnCustomTabsSession(
        session: CustomTabsSession
    )

    fun pickService(
        id: StreamingServiceID,
        activityResultLauncher: ActivityResultLauncher<Intent>,
        customTabsIntent: CustomTabsIntent,
        migrationOption: MigrationOption?
    )

    fun handleOAuthResult(
        intent: Intent
    )

    fun consumeMigrationOptionsDialog()

}

@HiltViewModel
internal class StreamingServicePickerViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val oAuth2UC: IOAuth2UC
) : BaseViewModel(savedStateHandle), IStreamingServicePickerViewModel {

    private val args = StreamingServicePickerFragmentArgs.fromBundle(savedStateHandle.toBundle())

    private val targetService = StreamingService.requireById(args.targetService)

    private val accessStateByService = MutableStateFlow(emptyMap<StreamingService, OAuth2State>())

    override val uiState: MutableStateFlow<SrcStreamingServicePickerUiState?> =
        MutableStateFlow(null)

    override val showMigrationOptionsDialog: MutableStateFlow<StreamingServiceID?> =
        MutableStateFlow(null)

    private var lastPickedService: StreamingService? = null

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

    private fun createUiState(
        accessStateByService: Map<StreamingService, OAuth2State>
    ): SrcStreamingServicePickerUiState {
        return SrcStreamingServicePickerUiState(
            serviceStates = StreamingService.values()
                .filter { streamingService ->
                    streamingService.isEnabled && streamingService != targetService
                }
                .map { streamingService ->
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

    override fun pickService(
        id: StreamingServiceID,
        activityResultLauncher: ActivityResultLauncher<Intent>,
        customTabsIntent: CustomTabsIntent,
        migrationOption: MigrationOption?
    ) {
        val pickedService = StreamingService.requireById(id)
        val accessState = requireNotNull(accessStateByService.value[pickedService])

        lastPickedService = pickedService

        if (accessState.accessToken == null) {
            viewModelScope.launch(Dispatchers.Default) {
                oAuth2UC.performOAuthRequest(
                    pickedService,
                    customTabsIntent,
                    activityResultLauncher
                )
            }
        } else {
            if (migrationOption != null) {
                navigateTo(
                    UserContentRootDeepLink.create(
                        pickedService.id,
                        targetService.id,
                        migrationOption is MigrationOption.All
                    ),
                    popTo = R.id.streamingServicesFragment
                )
            } else {
                showMigrationOptionsDialog.value = pickedService.id
            }
        }
    }

    override fun handleOAuthResult(
        intent: Intent
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            val pickedService = lastPickedService
            val oAuth2State = oAuth2UC.handleOAuthResult(intent)

            if (pickedService != null && oAuth2State?.accessToken != null) {
                showMigrationOptionsDialog.value = pickedService.id
            }
        }
    }

    override fun consumeMigrationOptionsDialog() {
        showMigrationOptionsDialog.value = null
    }

}
