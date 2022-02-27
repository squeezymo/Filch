package me.squeezymo.usercontent.impl.ui.data

import me.squeezymo.core.domain.data.ID

internal data class PlayerState(
    val id: ID,
    val url: String,
    val track: String,
    val artist: String?
)
