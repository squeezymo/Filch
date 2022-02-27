package me.squeezymo.core.ext

import android.os.Parcelable
import androidx.fragment.app.Fragment
import androidx.lifecycle.LiveData
import androidx.navigation.fragment.findNavController

fun <R : Parcelable> Fragment.getNavigationResult(
    key: String = "result"
): LiveData<R>? =
    findNavController()
        .currentBackStackEntry
        ?.savedStateHandle
        ?.getLiveData(key)

fun <R : Parcelable> Fragment.setNavigationResult(
    result: R
) {
    findNavController()
        .previousBackStackEntry
        ?.savedStateHandle
        ?.set("result", result)
}
