@file:Suppress("NOTHING_TO_INLINE")
package me.squeezymo.core.ext

import android.content.ClipData
import android.graphics.Outline
import android.view.View
import android.view.ViewOutlineProvider
import androidx.annotation.Px
import kotlin.math.roundToInt

@Px
fun View.dp(value: Int): Int = (value * resources.displayMetrics.density).roundToInt()

@Px
fun View.dp(value: Float): Int = (value * resources.displayMetrics.density).roundToInt()

inline fun View.visibleOrGone(condition: () -> Boolean) {
    visibility = if (condition()) View.VISIBLE else View.GONE
}

inline fun View.visibleOrInvisible(condition: () -> Boolean) {
    visibility = if (condition()) View.VISIBLE else View.INVISIBLE
}

fun View.clipWithRoundRect(cornerRadiusDp: Int) {
    clipToOutline = true
    outlineProvider = object : ViewOutlineProvider() {
        override fun getOutline(view: View, outline: Outline) {
            outline.setRoundRect(
                0,
                0,
                view.width,
                view.height,
                view.dp(cornerRadiusDp).toFloat()
            )
        }
    }
}

inline fun View.doOnSizeChanged(
    crossinline block: (width: Int, height: Int) -> Unit
) {
    addOnLayoutChangeListener { _, left, top, right, bottom, oldLeft, oldTop, oldRight, oldBottom ->
        val width = right - left
        val height = bottom - top
        val oldWidth = oldRight - oldLeft
        val oldHeight = oldBottom - oldTop

        if (width != oldWidth || height != oldHeight) {
            block(width, height)
        }
    }
}

inline fun View.startDragAndDropCompat(
    data: ClipData,
    shadowBuilder: View.DragShadowBuilder = View.DragShadowBuilder(this),
    myLocalState: Any = this,
    flags: Int = 0
) {
    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
        startDragAndDrop(data, shadowBuilder, myLocalState, flags)
    } else {
        @Suppress("DEPRECATION")
        startDrag(data, shadowBuilder, myLocalState, flags)
    }
}
