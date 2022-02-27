package me.squeezymo.core.ui.vmdelegate

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

interface IErrorHandlingVmDelegate<E> {

    val errorEvent: Flow<E?>

    fun showError(error: E)

    fun notifyOnErrorHandled()

}

abstract class ErrorHandlingVmDelegate<E>(): IErrorHandlingVmDelegate<E> {

    final override val errorEvent: MutableStateFlow<E?> =
        MutableStateFlow(null)

    final override fun showError(error: E) {
        errorEvent.value = error
    }

    final override fun notifyOnErrorHandled() {
        errorEvent.value = null
    }

}
