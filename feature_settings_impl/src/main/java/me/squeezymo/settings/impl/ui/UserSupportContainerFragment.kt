package me.squeezymo.settings.impl.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.navigation.navGraphViewModels
import dagger.hilt.android.AndroidEntryPoint
import me.squeezymo.analytics.api.DI_ANALYTICS_DELEGATE_FRAGMENT
import me.squeezymo.analytics.api.ui.delegate.IAnalyticsViewDelegate
import me.squeezymo.core.ui.BaseChildFragment
import me.squeezymo.settings.impl.R
import me.squeezymo.settings.impl.databinding.FUserSupportContainerBinding
import javax.inject.Inject
import javax.inject.Named

@AndroidEntryPoint
internal class UserSupportContainerFragment :
    BaseChildFragment<FUserSupportContainerBinding, IUserSupportContainerViewModel, ISettingsRootViewModel>() {

    override val viewModel: IUserSupportContainerViewModel
            by viewModels<UserSupportContainerViewModel>()

    override val rootViewModel: ISettingsRootViewModel
            by navGraphViewModels<SettingsRootViewModel>(R.id.nav_settings_graph)

    @Inject
    @Named(DI_ANALYTICS_DELEGATE_FRAGMENT)
    internal lateinit var analyticsDelegate: IAnalyticsViewDelegate

    override fun createViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): FUserSupportContainerBinding {
        return FUserSupportContainerBinding.inflate(inflater, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        analyticsDelegate.bind(viewLifecycleOwner) { "Помощь" }

        binding.backBtn.setOnClickListener {
            navigateUp()
        }
    }

}
