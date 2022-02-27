package me.squeezymo.usercontent.impl.ui.data

import me.squeezymo.core.domain.data.ID
import me.squeezymo.core.domain.data.PlaylistTitle

internal sealed class PlaylistsUiData {

    object Hide : PlaylistsUiData()

    data class Show(
        val playlistsSearchState: PlaylistsSearchState?,
        val playlistsMigrationState: Map<PlaylistTitle, PlaylistMigrationState>,
        val playlistsSelectedForMigration: Set<ID>
    ): PlaylistsUiData()

}
