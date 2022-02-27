package me.squeezymo.core.ui.viewdelegate

import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.launch
import me.squeezymo.core.ui.vmdelegate.IErrorHandlingVmDelegate

abstract class ErrorHandlingViewDelegate<E>(
    private val vmDelegate: IErrorHandlingVmDelegate<E>
) {

    abstract fun handleError(error: E)

    fun startObserving(fragment: Fragment) {
        fragment.viewLifecycleOwner.lifecycleScope.launch {
            fragment.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    vmDelegate.errorEvent.filterNotNull().collect { errEvent ->
                        try {
                            handleError(errEvent)
                        }
                        finally {
                            vmDelegate.notifyOnErrorHandled()
                        }
                    }
                }
            }
        }
    }

}
