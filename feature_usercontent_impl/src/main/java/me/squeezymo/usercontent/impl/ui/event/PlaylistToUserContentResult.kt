package me.squeezymo.usercontent.impl.ui.event

import me.squeezymo.core.domain.data.BasePlaylist
import me.squeezymo.usercontent.impl.ui.data.PlaylistMigrationState

internal data class PlaylistToUserContentResult(
    val srcPlaylist: BasePlaylist,
    val dstPlaylist: BasePlaylist?,
    val dstPlaylistMigrationState: PlaylistMigrationState?
)
