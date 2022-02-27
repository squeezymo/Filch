package me.squeezymo.usercontent.api.domain.usecase

import kotlinx.coroutines.flow.Flow
import me.squeezymo.core.domain.data.ID
import me.squeezymo.core.domain.data.StatelessResult
import me.squeezymo.core.domain.data.StreamingServiceID

interface IAddTracksToPlaylistUC {

    suspend fun addTracksToPlaylist(
        serviceId: StreamingServiceID,
        playlistId: ID,
        ids: Set<ID>
    ): Flow<Map<ID, StatelessResult>>

}
