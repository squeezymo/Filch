@file:Suppress("NOTHING_TO_INLINE")
package me.squeezymo.core.ext

import android.content.Context
import android.graphics.drawable.Drawable
import androidx.annotation.ColorInt
import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import androidx.core.content.ContextCompat

@ColorInt
inline fun Context.getColorCompat(@ColorRes id: Int): Int =
    ContextCompat.getColor(this, id)

inline fun Context.getDrawableCompat(@DrawableRes resId: Int): Drawable? =
    ContextCompat.getDrawable(this, resId)

inline fun Context.getDrawableOrThrow(@DrawableRes resId: Int): Drawable =
    requireNotNull(getDrawableCompat(resId)) { "Drawable $resId not found" }
