@file:Suppress("NOTHING_TO_INLINE")
package me.squeezymo.core.ext

import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.contract

@ExperimentalContracts
inline fun Int?.isNullOrZero(): Boolean {
    contract {
        returns(false) implies (this@isNullOrZero != null)
    }

    return this == null || this == 0
}
