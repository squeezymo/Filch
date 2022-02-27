package me.squeezymo.core.firebase.data

data class Cacheable<T>(
    val data: T,
    val isFromCache: Boolean
)
