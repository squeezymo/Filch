package me.squeezymo.usercontent.api.domain.usecase

import me.squeezymo.core.domain.data.BasePlaylist
import me.squeezymo.core.domain.data.StreamingServiceID

interface IGetPlaylistsUC {

    suspend fun getPlaylists(
        serviceId: StreamingServiceID,
        excludeExternalTrackIds: Boolean = false
    ): List<BasePlaylist>

}
