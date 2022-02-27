@file:Suppress("NOTHING_TO_INLINE")
package me.squeezymo.core.ext

import android.content.res.TypedArray
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

@ExperimentalContracts
inline fun TypedArray.useAndRecycle(
    block: TypedArray.() -> Unit
) {
    contract {
        callsInPlace(block, InvocationKind.EXACTLY_ONCE)
    }

    try {
        block(this)
    }
    finally {
        recycle()
    }
}
