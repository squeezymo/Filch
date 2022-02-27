package me.squeezymo.core.ext

import android.os.Build
import android.util.DisplayMetrics
import android.view.Window
import android.view.WindowInsets

data class ScreenDimensions(
    val widthPx: Int,
    val heightPx: Int
)

val Window.screenDimensions: ScreenDimensions
    get() {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val windowMetrics = windowManager.currentWindowMetrics
            val systemBarInsets = windowMetrics
                .windowInsets
                .getInsets(WindowInsets.Type.systemBars())
//            val imeInsets = windowMetrics
//                .windowInsets
//                .getInsets(WindowInsets.Type.ime())

            val width = windowMetrics.bounds.width() -
                    (systemBarInsets.left + systemBarInsets.right) /*-
                    (imeInsets.left + imeInsets.right)*/
            val height = windowMetrics.bounds.height() -
                    (systemBarInsets.top + systemBarInsets.bottom) /*-
                    (imeInsets.top + imeInsets.bottom)*/

            ScreenDimensions(width, height)
        } else {
            DisplayMetrics()
                .also { displayMetrics ->
                    @Suppress("DEPRECATION")
                    windowManager.defaultDisplay.getMetrics(displayMetrics)
                }
                .let { displayMetrics ->
                    @Suppress("DEPRECATION")
                    val horizontalInsetsPx =
                        decorView.rootWindowInsets?.let { it.systemWindowInsetLeft + it.systemWindowInsetRight } ?: 0
                    @Suppress("DEPRECATION", "SimpleRedundantLet")
                    val verticalInsetsPx =
                        decorView.rootWindowInsets?.let { it.systemWindowInsetTop/* + it.systemWindowInsetBottom*/ } ?: 0
                    ScreenDimensions(
                        displayMetrics.widthPixels - horizontalInsetsPx,
                        displayMetrics.heightPixels - verticalInsetsPx
                    )
                }
        }
    }
