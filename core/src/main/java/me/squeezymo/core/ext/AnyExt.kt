package me.squeezymo.core.ext

import com.google.gson.annotations.SerializedName
import java.lang.reflect.Modifier
import java.util.*

inline fun <reified T> Any?.castOrThrow(): T {
    if (this == null)
        throw NullPointerException("\"${T::class.java.canonicalName} expected but was \"null\"")

    return this as? T
        ?: throw ClassCastException("${T::class.java.canonicalName} expected but was ${(this.javaClass.canonicalName)}")
}

inline fun <reified T> Any?.castOrNull(): T? {
    return this as? T
}

fun Any.serializeToMap(): Map<String, Any> {
    val result: MutableMap<String, Any> = HashMap()

    for (field in javaClass.declaredFields) {
        if (Modifier.isStatic(field.modifiers)) {
            continue
        }

        field.isAccessible = true

        try {
            if (!Modifier.isTransient(field.modifiers)) {
                field[this].let { value ->
                    if (value != null) {
                        val key =
                            field.getAnnotation(SerializedName::class.java)?.value ?: field.name
                        result[key] = value
                    }
                }
            }
        } catch (e: IllegalAccessException) {
            // TODO Logs
        }
    }

    return result
}
