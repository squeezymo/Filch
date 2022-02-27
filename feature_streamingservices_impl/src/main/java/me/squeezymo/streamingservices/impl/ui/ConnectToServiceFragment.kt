package me.squeezymo.streamingservices.impl.ui

import android.app.Activity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.browser.customtabs.CustomTabsSession
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import me.squeezymo.analytics.api.DI_ANALYTICS_DELEGATE_FRAGMENT
import me.squeezymo.analytics.api.ui.delegate.IAnalyticsViewDelegate
import me.squeezymo.core.ui.BaseBottomSheetDialogFragment
import me.squeezymo.streamingservices.api.domain.ui.getBottomSheetBackgroundResId
import me.squeezymo.streamingservices.api.domain.ui.getIconResId
import me.squeezymo.streamingservices.api.domain.ui.getUiMigrationAction
import me.squeezymo.streamingservices.api.domain.ui.getUiName
import me.squeezymo.streamingservices.impl.R
import me.squeezymo.streamingservices.impl.databinding.FConnectToServiceBinding
import me.squeezymo.streamingservices.impl.ui.factory.CustomTabsFactory
import me.squeezymo.streamingservices.impl.ui.uistate.ConnectToServiceUiState
import javax.inject.Inject
import javax.inject.Named

@AndroidEntryPoint
internal class ConnectToServiceFragment :
    BaseBottomSheetDialogFragment<FConnectToServiceBinding, IConnectToServiceViewModel>() {

    override val viewModel: IConnectToServiceViewModel by viewModels<ConnectToServiceViewModel>()

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
    ): FConnectToServiceBinding {
        return FConnectToServiceBinding.inflate(inflater, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        analyticsDelegate.bind(viewLifecycleOwner) {
            "Стащить в ${viewModel.toService.getUiName(resources)}; " +
                    if (viewModel.uiState.value.isConnected) "авторизован" else "не авторизован"
        }

        viewModel.bindOAuthService(requireActivity())

        binding.root.setBackgroundResource(
            viewModel.toService.getBottomSheetBackgroundResId()
        )
        binding.root.clipToOutline = true

        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel
                        .uiState
                        .collect(::updateState)
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

    private fun updateState(state: ConnectToServiceUiState) {
        binding.serviceTitleTv.text = state.service.getUiName(resources)
        binding.serviceLogoIv.setImageResource(state.service.getIconResId())

        if (state.isConnected) {
            binding.descriptionTv.text =
                getString(R.string.connect_to_service_description_connected)
            binding.connectBtn.text =
                state.service.getUiMigrationAction(resources)
            binding.connectBtn.setOnClickListener {
                viewModel.openServicePicker()
            }
        } else {
            binding.descriptionTv.text =
                getString(R.string.connect_to_service_description_not_connected)
            binding.connectBtn.text =
                getString(R.string.connect_to_service_action_connect)
            binding.connectBtn.setOnClickListener {
                analyticsDelegate.appendScreenEvent(
                    "Войти в ${viewModel.toService.getUiName(resources)}"
                )
                viewModel.connect(
                    activityResultLauncher = performOauthAuthorization,
                    customTabsIntent = customTabsFactory
                        .createCustomTabsIntentBuilder(requireContext(), viewModel.toService.id)
                        .let { builder ->
                            val session = customTabsSession
                            if (session == null) builder else builder.setSession(session)
                        }
                        .build()
                )
            }
        }
    }

}
