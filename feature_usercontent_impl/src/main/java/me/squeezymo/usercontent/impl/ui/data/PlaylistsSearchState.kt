package me.squeezymo.usercontent.impl.ui.data

import me.squeezymo.core.domain.data.BasePlaylist
import me.squeezymo.core.domain.data.ID
import me.squeezymo.core.domain.data.PlaylistTitle

internal data class PlaylistsSearchState(
    val inProgress: Boolean,
    val playlistProgresses: List<PlaylistProgress>,
    val srcTitleToPlaylist: Map<PlaylistTitle, BasePlaylist>,
    val dstTitleToPlaylist: Map<PlaylistTitle, BasePlaylist>,
    val srcIdToDstIdWithinPlaylist: Map<ID, ID>
)
