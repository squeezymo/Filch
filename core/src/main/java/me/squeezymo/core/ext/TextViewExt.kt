package me.squeezymo.core.ext

import android.view.View
import android.widget.TextView

fun TextView.setTextOrMakeGone(text: String?) {
    if (text == null) {
        visibility = View.GONE
    }
    else {
        this.text = text
        visibility = View.VISIBLE
    }
}

fun TextView.setTextOrMakeInvisible(text: String?) {
    if (text == null) {
        visibility = View.INVISIBLE
    }
    else {
        this.text = text
        visibility = View.VISIBLE
    }
}
