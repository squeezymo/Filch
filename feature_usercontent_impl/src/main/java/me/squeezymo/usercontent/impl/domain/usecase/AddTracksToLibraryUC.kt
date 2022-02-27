package me.squeezymo.usercontent.impl.domain.usecase

import kotlinx.coroutines.flow.Flow
import me.squeezymo.core.domain.data.ID
import me.squeezymo.core.domain.data.StatelessResult
import me.squeezymo.core.domain.data.StreamingServiceID
import me.squeezymo.migrator.deezer.api.domain.IDeezerRepository
import me.squeezymo.migrator.spotify.api.domain.ISpotifyRepository
import me.squeezymo.migrator.youtube.api.domain.IYouTubeRepository
import me.squeezymo.streamingservices.api.domain.data.StreamingService
import me.squeezymo.usercontent.api.domain.usecase.IAddTracksToLibraryUC
import javax.inject.Inject

internal class AddTracksToLibraryUC @Inject constructor(
    private val youTubeRepo: IYouTubeRepository,
    private val spotifyRepo: ISpotifyRepository,
    private val deezerRepo: IDeezerRepository
) : IAddTracksToLibraryUC {

    private val workers = StreamingService.values().associate { service ->
        service to when (service) {
            StreamingService.VK ->
                Worker { TODO() }
            StreamingService.APPLE_MUSIC ->
                Worker { TODO() }
            StreamingService.YANDEX_MUSIC ->
                Worker { TODO() }
            StreamingService.SPOTIFY ->
                Worker(spotifyRepo::saveTracks)
            StreamingService.DEEZER ->
                Worker(deezerRepo::saveTracks)
            StreamingService.YOUTUBE ->
                Worker { TODO() }
        }
    }
    
    override suspend fun addTracksToLibrary(
        serviceId: StreamingServiceID,
        ids: Set<ID>
    ): Flow<Map<ID, StatelessResult>> {
        return requireNotNull(workers[StreamingService.requireById(serviceId)])
            .addTracksToLibrary(ids)
    }

    private class Worker(
        private val addTracks: suspend (ids: Set<ID>) -> Flow<Map<ID, StatelessResult>>
    ) {
        suspend fun addTracksToLibrary(
            ids: Set<ID>
        ): Flow<Map<ID, StatelessResult>> {
            return addTracks(ids)
        }
    }

}
