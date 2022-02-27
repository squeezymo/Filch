package me.squeezymo.usercontent.impl.ui.data

import androidx.annotation.IntRange
import me.squeezymo.core.domain.data.BaseTrack
import me.squeezymo.core.domain.data.EntityWithExternalIDs
import me.squeezymo.core.domain.data.ID

internal data class TracksSearchState(
    @IntRange(from = 0L, to = 100L) val progress: Int,
    val inProgress: Boolean,
    val srcIdToTrack: Map<ID, EntityWithExternalIDs<BaseTrack>>,
    val dstIdToTrack: Map<ID, EntityWithExternalIDs<BaseTrack>>,
    val srcIdToDstId: Map<ID, ID>
)
