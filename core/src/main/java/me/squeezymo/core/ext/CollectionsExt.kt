package me.squeezymo.core.ext

fun <K, V> Map<K?, V>.filterNotNullKeys(): Map<K, V> {
    val result = LinkedHashMap<K, V>()

    for ((key, value) in this) {
        if (key != null) {
            result[key] = value
        }
    }

    return result
}

fun <K, V> Map<K, V?>.filterNotNullValues(): Map<K, V> {
    val result = LinkedHashMap<K, V>()

    for ((key, value) in this) {
        if (value != null) {
            result[key] = value
        }
    }

    return result
}

inline fun <K, V, R> Map<out K, V>.mapNotNullValues(transform: (Map.Entry<K, V>) -> R?): Map<K, R> {
    return mapNotNullValuesTo(LinkedHashMap(), transform)
}

inline fun <K, V, R, C : MutableMap<K, in R>> Map<out K, V>.mapNotNullValuesTo(
    destination: C,
    transform: (Map.Entry<K, V>) -> R?
): C {
    entries.forEach { entry ->
        if (entry.value != null) {
            val mappedValue = transform(entry)

            if (mappedValue != null) {
                destination[entry.key] = mappedValue
            }
        }
    }

    return destination
}

inline fun <T> MutableList<T>.mapInPlace(
    transform: (T) -> T
) {
    forEachIndexed { index, elem ->
        set(index, transform(elem))
    }
}
