package me.squeezymo.core.ext

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update

inline fun <V> MutableStateFlow<Set<V>>.updateSet(
    block: (MutableSet<V>) -> Unit
) {
    update { items ->
        val mutableItems = items.toMutableSet()
        block(mutableItems)
        mutableItems
    }
}

inline fun <K, V> MutableStateFlow<Map<K, V>?>.updateNullableMap(
    block: (MutableMap<K, V>?) -> Unit
) {
    update { items ->
        val mutableItems = items?.toMutableMap()
        block(mutableItems)
        mutableItems
    }
}

inline fun <K, V> MutableStateFlow<Map<K, V>>.updateMap(
    block: (MutableMap<K, V>) -> Unit
) {
    update { items ->
        val mutableItems = items.toMutableMap()
        block(mutableItems)
        mutableItems
    }
}
