package me.squeezymo.core.ui

import android.os.Bundle
import android.view.KeyEvent
import android.view.View
import androidx.navigation.NavController
import androidx.viewbinding.ViewBinding
import com.google.android.material.bottomsheet.BottomSheetDialog
import me.squeezymo.core.ui.navigation.contract.HasInnerNavigation

abstract class BaseBottomSheetRootDialogFragment<VB : ViewBinding, VM : IBaseViewModel> :
    BaseBottomSheetDialogFragment<VB, VM>(),
    HasInnerNavigation {

    private lateinit var innerNavController: NavController

    abstract fun createInnerNavController(): NavController

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        innerNavController = createInnerNavController()
    }

    override fun setUpDialog(dialog: BottomSheetDialog) {
        super.setUpDialog(dialog)

        dialog.setOnKeyListener { _, keyCode, event ->
            // Handle back press.
            // `app:defaultNavHost="true"` does not work for dialog destinations.
            if (keyCode == KeyEvent.KEYCODE_BACK && event.action == KeyEvent.ACTION_UP) {
                navigateUp()
            }
            true
        }
    }

    final override fun canNavigateUp(): Boolean {
        return innerNavController.previousBackStackEntry != null
    }

    final override fun navigateUp() {
        if (!innerNavController.navigateUp()) {
            dialog?.dismiss()
        }
    }

}
