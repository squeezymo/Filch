package me.squeezymo.usercontent.impl.domain.data

import me.squeezymo.core.domain.data.ID

data class CompositeTrackId(
    val srcTrackId: ID,
    val dstTrackId: ID?
)
