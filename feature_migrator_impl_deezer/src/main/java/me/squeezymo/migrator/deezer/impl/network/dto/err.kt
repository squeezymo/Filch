package me.squeezymo.migrator.deezer.impl.network.dto

internal data class DeezerApiErrorDTO(
    val type: String,
    val message: String,
    val code: Int
)
