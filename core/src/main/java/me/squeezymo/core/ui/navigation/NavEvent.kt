package me.squeezymo.core.ui.navigation

import android.net.Uri
import androidx.annotation.IdRes
import androidx.navigation.NavDirections

sealed class NavEvent {

    class ByUri(
        val uri: Uri,
        @IdRes val popTo: Int?
    ) : NavEvent()

    class ByNavDirections(
        val directions: NavDirections
    ) : NavEvent()

}
