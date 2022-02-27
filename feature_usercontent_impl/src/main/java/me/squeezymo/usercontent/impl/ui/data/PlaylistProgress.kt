package me.squeezymo.usercontent.impl.ui.data

import me.squeezymo.core.domain.data.BasePlaylist

internal data class PlaylistProgress(
    val playlist: BasePlaylist,
    val inProgress: Boolean
)
