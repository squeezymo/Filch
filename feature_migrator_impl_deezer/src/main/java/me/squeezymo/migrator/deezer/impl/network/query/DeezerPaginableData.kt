package me.squeezymo.migrator.deezer.impl.network.query

class DeezerPaginableData<P : Any>(
    val data: List<P>,
    val next: String?
)
