package me.squeezymo.usercontent.impl.domain.usecase

import kotlinx.coroutines.flow.Flow
import me.squeezymo.core.domain.data.ID
import me.squeezymo.core.domain.data.StatelessResult
import me.squeezymo.core.domain.data.StreamingServiceID
import me.squeezymo.migrator.deezer.api.domain.IDeezerRepository
import me.squeezymo.migrator.spotify.api.domain.ISpotifyRepository
import me.squeezymo.migrator.youtube.api.domain.IYouTubeRepository
import me.squeezymo.streamingservices.api.domain.data.StreamingService
import me.squeezymo.usercontent.api.domain.usecase.IAddTracksToPlaylistUC
import javax.inject.Inject

internal class AddTracksToPlaylistUC @Inject constructor(
    private val youTubeRepo: IYouTubeRepository,
    private val spotifyRepo: ISpotifyRepository,
    private val deezerRepo: IDeezerRepository
) : IAddTracksToPlaylistUC {

    private val workers = StreamingService.values().associate { service ->
        service to when (service) {
            StreamingService.VK ->
                Worker { _, _ -> TODO() }
            StreamingService.APPLE_MUSIC ->
                Worker { _, _ -> TODO() }
            StreamingService.YANDEX_MUSIC ->
                Worker { _, _ -> TODO() }
            StreamingService.SPOTIFY ->
                Worker(spotifyRepo::saveTracksToPlaylist)
            StreamingService.DEEZER ->
                Worker(deezerRepo::saveTracksToPlaylist)
            StreamingService.YOUTUBE ->
                Worker { _, _ -> TODO() }
        }
    }

    override suspend fun addTracksToPlaylist(
        serviceId: StreamingServiceID,
        playlistId: ID,
        ids: Set<ID>
    ): Flow<Map<ID, StatelessResult>> {
        return requireNotNull(workers[StreamingService.requireById(serviceId)])
            .addTracksToPlaylist(playlistId, ids)
    }

    private class Worker(
        val addTracksToPlaylist: suspend (
            playlistId: ID,
            ids: Set<ID>
        ) -> Flow<Map<ID, StatelessResult>>
    )

}
