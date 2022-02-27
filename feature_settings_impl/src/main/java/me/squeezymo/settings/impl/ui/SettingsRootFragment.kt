package me.squeezymo.settings.impl.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import dagger.hilt.android.AndroidEntryPoint
import me.squeezymo.core.ui.BaseBottomSheetRootDialogFragment
import me.squeezymo.core.ui.navigation.contract.HasInnerNavigation
import me.squeezymo.settings.impl.R
import me.squeezymo.settings.impl.databinding.FSettingsRootBinding

@AndroidEntryPoint
internal class SettingsRootFragment :
    BaseBottomSheetRootDialogFragment<FSettingsRootBinding, ISettingsRootViewModel>(),
    HasInnerNavigation {

    override val viewModel: ISettingsRootViewModel by viewModels<SettingsRootViewModel>()

    override fun createViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): FSettingsRootBinding {
        return FSettingsRootBinding.inflate(inflater, container, false)
    }

    override fun createInnerNavController(): NavController {
        val navHostFragment: NavHostFragment = binding.navHostSettingsFragment.getFragment()
        val innerNavController = navHostFragment.navController
        innerNavController.setGraph(
            R.navigation.nav_internal_settings
        )

        return innerNavController
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.contentContainer.clipToOutline = true
    }

}
