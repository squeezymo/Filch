package me.squeezymo.usercontent.impl.ui.data

import me.squeezymo.core.domain.data.BaseTrack
import me.squeezymo.core.domain.data.EntityWithExternalIDs
import me.squeezymo.core.domain.data.ID

internal data class IdToTrack(
    val srcIdToTrack: Map<ID, EntityWithExternalIDs<BaseTrack>>,
    val dstIdToTrack: Map<ID, EntityWithExternalIDs<BaseTrack>>
)
