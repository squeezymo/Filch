package me.squeezymo.streamingservices.impl.ui

import android.app.Activity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.browser.customtabs.CustomTabsSession
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.launch
import me.squeezymo.analytics.api.DI_ANALYTICS_DELEGATE_FRAGMENT
import me.squeezymo.analytics.api.ui.delegate.IAnalyticsViewDelegate
import me.squeezymo.core.domain.data.StreamingServiceID
import me.squeezymo.core.ui.BaseBottomSheetDialogFragment
import me.squeezymo.core.uicomponents.dialog.FilchAlertDialogBuilder
import me.squeezymo.streamingservices.api.domain.data.StreamingService
import me.squeezymo.streamingservices.api.domain.ui.getUiName
import me.squeezymo.streamingservices.impl.R
import me.squeezymo.streamingservices.impl.databinding.FStreamingServicePickerBinding
import me.squeezymo.streamingservices.impl.ui.data.MigrationOption
import me.squeezymo.streamingservices.impl.ui.factory.CustomTabsFactory
import me.squeezymo.streamingservices.impl.ui.widget.SrcStreamingServicePickerUiState
import javax.inject.Inject
import javax.inject.Named

@AndroidEntryPoint
internal class StreamingServicePickerFragment :
    BaseBottomSheetDialogFragment<FStreamingServicePickerBinding, IStreamingServicePickerViewModel>() {

    override val viewModel: IStreamingServicePickerViewModel by viewModels<StreamingServicePickerViewModel>()

    override val isFullHeight: Boolean = false

    private val performOauthAuthorization = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result: ActivityResult ->
        if (result.resultCode == Activity.RESULT_OK) {
            analyticsDelegate.appendEvent("Авторизация успешна")
            viewModel.handleOAuthResult(result.data!!)
        }
        else {
            analyticsDelegate.appendEvent("Авторизация не успешна или отменена пользователем")
        }
    }

    @Inject
    internal lateinit var customTabsFactory: CustomTabsFactory

    private var customTabsSession: CustomTabsSession? = null

    @Inject
    @Named(DI_ANALYTICS_DELEGATE_FRAGMENT)
    internal lateinit var analyticsDelegate: IAnalyticsViewDelegate

    override fun createViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): FStreamingServicePickerBinding {
        return FStreamingServicePickerBinding.inflate(inflater, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        analyticsDelegate.bind(viewLifecycleOwner) {
            "Выбор сервиса"
        }

        binding.srcPickerWidget.onServiceClickListener = { serviceId ->
            pickService(serviceId, migrationOption = null)
        }

        viewModel.bindOAuthService(requireActivity())

        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.uiState.collect(::updateSrcStreamingServiceState)
                }

                launch {
                    viewModel
                        .showMigrationOptionsDialog
                        .filterNotNull()
                        .collect(::showMigrationOptionsDialog)
                }

                launch {
                    requestNewCustomTabsSession()?.let { newCustomTabsSession ->
                        customTabsSession = newCustomTabsSession
                        viewModel.notifyOnCustomTabsSession(newCustomTabsSession)
                    }
                }
            }
        }
    }

    private fun updateSrcStreamingServiceState(
        srcStreamingServiceUiState: SrcStreamingServicePickerUiState?
    ) {
        if (srcStreamingServiceUiState == null) {
            return
        }

        binding.srcPickerWidget.setState(srcStreamingServiceUiState)
    }

    private fun showMigrationOptionsDialog(serviceId: StreamingServiceID) {
        viewModel.consumeMigrationOptionsDialog()

        var dialog: AlertDialog? = null

        dialog = FilchAlertDialogBuilder(
            requireContext(),
            text = getString(R.string.streaming_service_picker_migrate_header),
            buttons = listOf(
                FilchAlertDialogBuilder.Button(
                    text = getString(R.string.streaming_service_picker_migrate_option_all),
                    isBold = true,
                    clickListener = {
                        dialog?.dismiss()
                        pickService(serviceId, MigrationOption.All)
                    }
                ),
                FilchAlertDialogBuilder.Button(
                    text = getString(R.string.streaming_service_picker_migrate_option_selected),
                    clickListener = {
                        dialog?.dismiss()
                        pickService(serviceId, MigrationOption.Manual)
                    }
                ),
                FilchAlertDialogBuilder.Button(
                    text = getString(R.string.streaming_service_picker_migrate_option_cancel),
                    clickListener = {
                        dialog?.dismiss()
                    }
                ),
            )
        ).build().show()
    }

    private fun pickService(
        serviceId: StreamingServiceID,
        migrationOption: MigrationOption?
    ) {
        analyticsDelegate.appendScreenEvent(
            "Стащить из ${StreamingService.requireById(serviceId).getUiName(resources)}"
        )
        viewModel.pickService(
            id = serviceId,
            activityResultLauncher = performOauthAuthorization,
            customTabsIntent = customTabsFactory
                .createCustomTabsIntentBuilder(requireContext(), serviceId)
                .let { builder ->
                    val session = customTabsSession
                    if (session == null) builder else builder.setSession(session)
                }
                .build(),
            migrationOption = migrationOption
        )
    }

}
