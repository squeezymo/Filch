package me.squeezymo.core.ext

import android.content.SharedPreferences

inline fun SharedPreferences.editAndApply(
    block: SharedPreferences.Editor.() -> SharedPreferences.Editor
) {
    block(edit()).apply()
}

fun SharedPreferences.clear() {
    editAndApply {
        clear()
    }
}
