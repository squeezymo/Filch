package me.squeezymo.usercontent.impl.ui

import androidx.navigation.navGraphViewModels
import androidx.viewbinding.ViewBinding
import me.squeezymo.core.ui.BaseChildFragment
import me.squeezymo.core.ui.IBaseViewModel
import me.squeezymo.usercontent.impl.R

internal abstract class BaseUserContentChildFragment<VB : ViewBinding, VM : IBaseViewModel> :
    BaseChildFragment<VB, VM, IUserContentRootViewModel>() {

    override val rootViewModel: IUserContentRootViewModel
            by navGraphViewModels<UserContentRootViewModel>(R.id.nav_user_content_graph)

}
