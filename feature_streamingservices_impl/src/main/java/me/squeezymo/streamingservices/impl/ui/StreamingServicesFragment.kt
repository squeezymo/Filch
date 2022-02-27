package me.squeezymo.streamingservices.impl.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import me.squeezymo.core.ui.BaseFragment
import me.squeezymo.core.uicomponents.dialog.FilchAlertDialogBuilder
import me.squeezymo.streamingservices.impl.R
import me.squeezymo.streamingservices.impl.databinding.FStreamingServicesBinding
import me.squeezymo.streamingservices.impl.ui.data.MigrationOption

@AndroidEntryPoint
internal class StreamingServicesFragment :
    BaseFragment<FStreamingServicesBinding, IStreamingServicesViewModel>() {

    override val viewModel: IStreamingServicesViewModel by viewModels<StreamingServicesViewModel>()

    override fun createViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): FStreamingServicesBinding {
        return FStreamingServicesBinding.inflate(inflater, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.bindOAuthServices(requireActivity())

        binding.streamingServicePicker.onServiceClickListener = { serviceId ->
            viewModel.navigateToService(serviceId)
        }
        binding.streamingServicePicker.onTryToMigrateListener = { from, to ->
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
                            viewModel.tryToMigrate(from, to, MigrationOption.All)
                        }
                    ),
                    FilchAlertDialogBuilder.Button(
                        text = getString(R.string.streaming_service_picker_migrate_option_selected),
                        clickListener = {
                            dialog?.dismiss()
                            viewModel.tryToMigrate(from, to, MigrationOption.Manual)
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

        binding.goToSettingsTv.setOnClickListener {
            viewModel.goToSettings()
        }

        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.updateOAuthTokens()

                launch {
                    viewModel.pickerUiState.collect { pickerUiState ->
                        binding.streamingServicePicker.setState(pickerUiState)
                    }
                }

                launch {
                    requestNewCustomTabsSession()?.let(viewModel::notifyOnCustomTabsSession)
                }
            }
        }
    }

}
