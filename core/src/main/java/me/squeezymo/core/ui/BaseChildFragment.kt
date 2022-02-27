package me.squeezymo.core.ui

import androidx.navigation.fragment.findNavController
import androidx.viewbinding.ViewBinding

abstract class BaseChildFragment<VB : ViewBinding, VM : IBaseViewModel, PVM : IBaseViewModel> :
    BaseFragment<VB, VM>() {

    protected abstract val rootViewModel: PVM

    protected fun navigateUp() {
        findNavController().navigateUp()
    }

}
