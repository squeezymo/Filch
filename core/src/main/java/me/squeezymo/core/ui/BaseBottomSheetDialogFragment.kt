package me.squeezymo.core.ui

import android.app.Dialog
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.PorterDuff
import android.net.Uri
import android.os.Bundle
import android.view.*
import android.widget.FrameLayout
import androidx.annotation.CallSuper
import androidx.browser.customtabs.CustomTabsSession
import androidx.core.view.updateLayoutParams
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.NavDeepLinkRequest
import androidx.navigation.NavDirections
import androidx.navigation.fragment.findNavController
import androidx.viewbinding.ViewBinding
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.launch
import me.squeezymo.core.R
import me.squeezymo.core.ui.contract.SupportsCustomTabs
import me.squeezymo.core.ui.navigation.NavEvent


abstract class BaseBottomSheetDialogFragment<VB : ViewBinding, VM : IBaseViewModel> :
    BottomSheetDialogFragment(),
    SupportsCustomTabs {

    private lateinit var behavior: BottomSheetBehavior<FrameLayout>

    private var _binding: VB? = null
    protected val binding: VB
        get() = checkNotNull(_binding) {
            "Binding can be used only between onCreateView() and onDestroyView(). " +
                    "Current state is ${lifecycle.currentState}"
        }

    open val isFullHeight: Boolean = true

    abstract val viewModel: VM

    @CallSuper
    protected open fun setUpDialog(dialog: BottomSheetDialog) {
        dialog
            .window
            ?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)

        if (isFullHeight) {
            dialog.setOnShowListener {
                dialog
                    .findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)
                    ?.updateLayoutParams<ViewGroup.LayoutParams> {
                        behavior.isFitToContents = false
                        height = WindowManager.LayoutParams.MATCH_PARENT
                    }
            }
        }
    }

    abstract fun createViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): VB

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return super.onCreateDialog(savedInstanceState).also { dialog ->
            dialog as BottomSheetDialog

            this.behavior = dialog.behavior
            this.behavior.skipCollapsed = true

            setUpDialog(dialog)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = createViewBinding(
            inflater.cloneInContext(
                ContextThemeWrapper(requireActivity(), R.style.Theme_Filch)
            ),
            container,
            savedInstanceState
        )

        return binding.root
    }

    @CallSuper
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val bottomSheet = view.parent as View

        bottomSheet.fitsSystemWindows = true
        bottomSheet.backgroundTintMode = PorterDuff.Mode.CLEAR
        bottomSheet.backgroundTintList = ColorStateList.valueOf(Color.TRANSPARENT)
        bottomSheet.setBackgroundColor(Color.TRANSPARENT)

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
        val navController = findNavController()

        navController
            .navigate(directions)
    }

    override fun onStart() {
        super.onStart()

        if (behavior.state != BottomSheetBehavior.STATE_EXPANDED) {
            behavior.state = BottomSheetBehavior.STATE_EXPANDED
        }
    }

    override suspend fun requestNewCustomTabsSession(): CustomTabsSession? {
        val activity = requireActivity()

        return if (activity is SupportsCustomTabs) {
            activity.requestNewCustomTabsSession()
        } else {
            null
        }
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }

}
