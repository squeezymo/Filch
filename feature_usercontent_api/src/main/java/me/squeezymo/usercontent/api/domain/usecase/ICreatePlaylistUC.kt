package me.squeezymo.usercontent.api.domain.usecase

import me.squeezymo.core.domain.data.BasePlaylist
import me.squeezymo.core.domain.data.PlaylistTitle
import me.squeezymo.core.domain.data.StreamingServiceID

interface ICreatePlaylistUC {

    suspend fun createPlaylist(
        serviceId: StreamingServiceID,
        playlistTitle: PlaylistTitle
    ): BasePlaylist

}
