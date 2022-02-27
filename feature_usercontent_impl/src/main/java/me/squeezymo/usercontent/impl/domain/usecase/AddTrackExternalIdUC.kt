package me.squeezymo.usercontent.impl.domain.usecase

import me.squeezymo.core.domain.data.ID
import me.squeezymo.core.domain.data.StreamingServiceID
import me.squeezymo.migrator.deezer.api.domain.IDeezerRepository
import me.squeezymo.migrator.spotify.api.domain.ISpotifyRepository
import me.squeezymo.migrator.youtube.api.domain.IYouTubeRepository
import me.squeezymo.streamingservices.api.domain.data.StreamingService
import me.squeezymo.usercontent.api.domain.usecase.IAddTrackExternalIdUC
import javax.inject.Inject

internal class AddTrackExternalIdUC @Inject constructor(
    private val youTubeRepo: IYouTubeRepository,
    private val spotifyRepo: ISpotifyRepository,
    private val deezerRepo: IDeezerRepository
) : IAddTrackExternalIdUC {

    private val workers = StreamingService.values().associate { service ->
        service to when (service) {
            StreamingService.VK ->
                Worker { _, _ -> TODO() }
            StreamingService.APPLE_MUSIC ->
                Worker { _, _ -> TODO() }
            StreamingService.YANDEX_MUSIC ->
                Worker { _, _ -> TODO() }
            StreamingService.SPOTIFY ->
                Worker(spotifyRepo::addExternalTrackId)
            StreamingService.DEEZER ->
                Worker(deezerRepo::addExternalTrackId)
            StreamingService.YOUTUBE ->
                Worker { _, _ -> TODO() }
        }
    }

    override suspend fun addTrackExternalId(
        serviceId: StreamingServiceID,
        trackId: ID,
        externalIds: Map<StreamingServiceID, ID>
    ) {
        return requireNotNull(workers[StreamingService.requireById(serviceId)])
            .addTrackExternalId(trackId, externalIds)
    }

    private class Worker(
        private val request: suspend (
            trackId: ID,
            externalIds: Map<StreamingServiceID, ID>
        ) -> Unit
    ) {
        suspend fun addTrackExternalId(
            trackId: ID,
            externalIds: Map<StreamingServiceID, ID>
        ) {
            return request(trackId, externalIds)
        }
    }

}
