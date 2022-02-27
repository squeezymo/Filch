package me.squeezymo.usercontent.impl.ui.data

import me.squeezymo.core.domain.data.ID
import me.squeezymo.usercontent.impl.domain.data.CompositeTrackId

internal data class PlaylistTracksMigrationState(
    val playlistId: ID,
    val tracksMigrationState: Map<CompositeTrackId, TrackMigrationState>,
    val playlistMigrationState: PlaylistMigrationState
)
