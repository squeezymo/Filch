package me.squeezymo.core.ui

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.CallSuper
import androidx.browser.customtabs.CustomTabsSession
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.NavDeepLinkRequest
import androidx.navigation.NavDirections
import androidx.navigation.fragment.findNavController
import androidx.viewbinding.ViewBinding
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.launch
import me.squeezymo.core.ui.contract.SupportsCustomTabs
import me.squeezymo.core.ui.navigation.NavEvent

abstract class BaseFragment<VB : ViewBinding, VM : IBaseViewModel> :
    Fragment(),
    SupportsCustomTabs {

    private var _binding: VB? = null
    protected val binding: VB
        get() = checkNotNull(_binding) {
            "Binding can be used only between onCreateView() and onDestroyView(). " +
                    "Current state is ${lifecycle.currentState}"
        }

    abstract val viewModel: VM

    abstract fun createViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): VB

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = createViewBinding(inflater, container, savedInstanceState)
        return binding.root
    }

    @CallSuper
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.navigationEvent.filterNotNull().collect { navEvent ->
                        try {
                            when (navEvent) {
                                is NavEvent.ByUri -> {
                                    handleUriNavigationEvent(navEvent.uri, navEvent.popTo)
                                }
                                is NavEvent.ByNavDirections -> {
                                    handleDirectionsNavigationEvent(navEvent.directions)
                                }
                            }
                        }
                        finally {
                            viewModel.notifyOnNavigationEventHandled()
                        }
                    }
                }
            }
        }
    }

    protected open fun handleUriNavigationEvent(uri: Uri, popTo: Int?) {
        val navController = findNavController()

        if (popTo != null) {
            navController.popBackStack(popTo, false)
        }

        navController.navigate(
            NavDeepLinkRequest.Builder
                .fromUri(uri)
                .build()
        )
    }

    protected open fun handleDirectionsNavigationEvent(directions: NavDirections) {
        findNavController().navigate(directions)
    }

    override suspend fun requestNewCustomTabsSession(): CustomTabsSession? {
        val activity = requireActivity()

        return if (activity is SupportsCustomTabs) {
            activity.requestNewCustomTabsSession()
        }
        else {
            null
        }
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }

}
