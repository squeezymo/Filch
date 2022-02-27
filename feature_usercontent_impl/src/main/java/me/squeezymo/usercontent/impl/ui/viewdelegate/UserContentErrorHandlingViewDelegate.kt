package me.squeezymo.usercontent.impl.ui.viewdelegate

import androidx.fragment.app.Fragment
import me.squeezymo.core.ui.viewdelegate.ErrorHandlingViewDelegate
import me.squeezymo.core.ui.vmdelegate.IErrorHandlingVmDelegate
import me.squeezymo.usercontent.impl.data.UserContentError

internal fun Fragment.addErrorHandlingViewDelegate(
    vmDelegate: IErrorHandlingVmDelegate<UserContentError>,
    handle: (UserContentError) -> Unit
): ErrorHandlingViewDelegate<UserContentError> {
    return object : ErrorHandlingViewDelegate<UserContentError>(vmDelegate) {
        override fun handleError(error: UserContentError) {
            handle(error)
        }
    }.also {
        it.startObserving(this)
    }
}
