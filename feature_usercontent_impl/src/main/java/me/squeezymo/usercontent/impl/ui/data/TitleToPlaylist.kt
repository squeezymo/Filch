package me.squeezymo.usercontent.impl.ui.data

import me.squeezymo.core.domain.data.BasePlaylist
import me.squeezymo.core.domain.data.PlaylistTitle

internal data class TitleToPlaylist(
    val srcTitleToPlaylist: Map<PlaylistTitle, BasePlaylist>,
    val dstTitleToPlaylist: Map<PlaylistTitle, BasePlaylist>
)
