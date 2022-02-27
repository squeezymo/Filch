package me.squeezymo.core.ui

import android.net.Uri
import androidx.annotation.IdRes
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.navigation.NavDirections
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import me.squeezymo.core.ui.navigation.NavEvent

interface IBaseViewModel {

    val navigationEvent: Flow<NavEvent?>

    fun navigateTo(uri: Uri, @IdRes popTo: Int? = null)

    fun navigateTo(directions: NavDirections)

    fun notifyOnNavigationEventHandled()

}

abstract class BaseViewModel(
    protected val savedStateHandle: SavedStateHandle
) : ViewModel(), IBaseViewModel {

    // Using extra property serves to avoid exposing mutability to ancestors
    private val _navigationEvent: MutableStateFlow<NavEvent?> =
        MutableStateFlow(null)
    final override val navigationEvent: StateFlow<NavEvent?> = _navigationEvent

    final override fun navigateTo(uri: Uri, @IdRes popTo: Int?) {
        _navigationEvent.value = NavEvent.ByUri(uri, popTo)
    }

    final override fun navigateTo(directions: NavDirections) {
        _navigationEvent.value = NavEvent.ByNavDirections(directions)
    }

    final override fun notifyOnNavigationEventHandled() {
        _navigationEvent.value = null
    }

}
