package me.squeezymo.usercontent.impl.ui

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
import me.squeezymo.streamingservices.api.domain.ui.getBottomSheetBackgroundResId
import me.squeezymo.usercontent.impl.R
import me.squeezymo.usercontent.impl.databinding.FUserContentRootBinding

@AndroidEntryPoint
internal class UserContentRootFragment :
    BaseBottomSheetRootDialogFragment<FUserContentRootBinding, IUserContentRootViewModel>(),
    HasInnerNavigation {

    override val viewModel: IUserContentRootViewModel by viewModels<UserContentRootViewModel>()

    override fun createViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): FUserContentRootBinding {
        return FUserContentRootBinding.inflate(inflater, container, false)
    }

    override fun createInnerNavController(): NavController {
        val navHostFragment: NavHostFragment = binding.navHostUserContentFragment.getFragment()
        val innerNavController = navHostFragment.navController
        innerNavController.setGraph(
            R.navigation.nav_internal_user_content,
            UserContentFragmentArgs(
                from = viewModel.fromService.id,
                to = viewModel.toService.id,
                autoMigrate = viewModel.shouldAutoMigrate
            ).toBundle()
        )

        return innerNavController
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.root.setBackgroundResource(
            viewModel.toService.getBottomSheetBackgroundResId()
        )
        binding.root.clipToOutline = true
    }

}
