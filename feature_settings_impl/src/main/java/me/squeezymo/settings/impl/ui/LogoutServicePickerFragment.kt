package me.squeezymo.settings.impl.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.launch
import me.squeezymo.core.ui.BaseBottomSheetDialogFragment
import me.squeezymo.core.uicomponents.dialog.FilchAlertDialogBuilder
import me.squeezymo.settings.impl.R
import me.squeezymo.settings.impl.databinding.FLogoutStreamingServicePickerBinding
import me.squeezymo.settings.impl.ui.event.LogoutResultEvent
import me.squeezymo.settings.impl.ui.widget.LogoutStreamingServicePickerUiState
import me.squeezymo.streamingservices.api.domain.ui.getUiName

@AndroidEntryPoint
internal class LogoutServicePickerFragment :
    BaseBottomSheetDialogFragment<FLogoutStreamingServicePickerBinding, ILogoutStreamingServicePickerViewModel>() {

    override val viewModel: ILogoutStreamingServicePickerViewModel
            by viewModels<LogoutStreamingServicePickerViewModel>()

    override val isFullHeight: Boolean = false

    override fun createViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): FLogoutStreamingServicePickerBinding {
        return FLogoutStreamingServicePickerBinding.inflate(inflater, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.logoutPickerWidget.onServiceClickListener = { serviceId ->
            viewModel.pickService(serviceId)
        }

        viewModel.bindOAuthService(requireActivity())

        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.uiState.collect(::updateLogoutStreamingServiceState)
                }

                launch {
                    viewModel.logoutResultEvent.filterNotNull().collect { logoutResultEvent ->
                        try {
                            handleLogoutResultEvent(logoutResultEvent)
                        }
                        finally {
                            viewModel.notifyOnLogoutResultHandled()
                        }
                    }
                }
            }
        }
    }

    private fun updateLogoutStreamingServiceState(
        logoutStreamingServiceUiState: LogoutStreamingServicePickerUiState?
    ) {
        if (logoutStreamingServiceUiState == null) {
            return
        }

        binding.logoutPickerWidget.setState(logoutStreamingServiceUiState)
    }

    private fun handleLogoutResultEvent(event: LogoutResultEvent) {
        findNavController().navigateUp()

        var dialog: AlertDialog? = null

        dialog = FilchAlertDialogBuilder(
            requireActivity(),
            text = if (event.isSuccess) {
                getString(
                    R.string.settings_caption_logout_success,
                    event.streamingService.getUiName(resources)
                )
            } else {
                getString(
                    R.string.settings_caption_logout_failure,
                    event.streamingService.getUiName(resources)
                )
            },
            buttons = listOf(
                FilchAlertDialogBuilder.Button(
                    text = getString(R.string.general_action_close),
                    isBold = true,
                    clickListener = {
                        dialog?.dismiss()
                    }
                ),
            )
        ).build().show()
    }

}
