package me.squeezymo.core.ext

import android.os.Bundle
import android.os.Parcelable
import androidx.lifecycle.SavedStateHandle
import java.io.Serializable

fun SavedStateHandle.toBundle(): Bundle {
    val bundle = Bundle()

    keys().forEach { key ->
        val value: Any? = get(key)

        if (value != null) {
            when (value) {
                is Parcelable -> {
                    bundle.putParcelable(key, value)
                }
                is Serializable -> {
                    bundle.putSerializable(key, value)
                }
                else -> {
                    throw IllegalStateException("Cannot put value of type ${value::class.java.canonicalName} into bundle")
                }
            }
        }
    }

    return bundle

}