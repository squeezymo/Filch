package me.squeezymo.usercontent.impl.domain.usecase

import me.squeezymo.core.domain.data.BaseTrack
import me.squeezymo.core.domain.data.EntityWithExternalIDs
import me.squeezymo.core.domain.data.StreamingServiceID
import me.squeezymo.migrator.api.domain.mapper.IBaseTrackDomainMapper
import me.squeezymo.migrator.deezer.api.domain.IDeezerRepository
import me.squeezymo.migrator.deezer.api.domain.data.DeezerTrack
import me.squeezymo.migrator.spotify.api.domain.ISpotifyRepository
import me.squeezymo.migrator.spotify.api.domain.data.SpotifyTrack
import me.squeezymo.migrator.youtube.api.domain.IYouTubeRepository
import me.squeezymo.migrator.youtube.api.domain.data.YoutubeTrack
import me.squeezymo.streamingservices.api.domain.data.StreamingService
import me.squeezymo.usercontent.api.domain.usecase.IGetTracksUC
import javax.inject.Inject

internal class GetTracksUC @Inject constructor(
    private val youTubeRepo: IYouTubeRepository,
    private val youtubeBaseMapper: IBaseTrackDomainMapper<YoutubeTrack>,
    private val spotifyRepo: ISpotifyRepository,
    private val spotifyBaseMapper: IBaseTrackDomainMapper<SpotifyTrack>,
    private val deezerRepo: IDeezerRepository,
    private val deezerBaseMapper: IBaseTrackDomainMapper<DeezerTrack>
) : IGetTracksUC {

    private val workers = StreamingService.values().associate { service ->
        service to when (service) {
            StreamingService.VK ->
                Worker({ TODO() }, DummyBaseDomainMapper())
            StreamingService.APPLE_MUSIC ->
                Worker({ TODO() }, DummyBaseDomainMapper())
            StreamingService.YANDEX_MUSIC ->
                Worker({ TODO() }, DummyBaseDomainMapper())
            StreamingService.SPOTIFY ->
                Worker(spotifyRepo::requestTracks, spotifyBaseMapper)
            StreamingService.DEEZER ->
                Worker(deezerRepo::requestTracks, deezerBaseMapper)
            StreamingService.YOUTUBE ->
                Worker(youTubeRepo::requestTracks, youtubeBaseMapper)
        }
    }

    override suspend fun getTracks(
        serviceId: StreamingServiceID,
        excludeExternalIds: Boolean
    ): List<EntityWithExternalIDs<BaseTrack>> {
        return requireNotNull(workers[StreamingService.requireById(serviceId)])
            .requestTracks(excludeExternalIds)
    }

    private class Worker<T>(
        private val request: suspend (excludeExternalIds: Boolean) -> List<EntityWithExternalIDs<T>>,
        private val mapper: IBaseTrackDomainMapper<T>
    ) {
        suspend fun requestTracks(
            excludeExternalIds: Boolean
        ): List<EntityWithExternalIDs<BaseTrack>> {
            return request(excludeExternalIds).map { trackEntity ->
                trackEntity.map(mapper::mapTrack)
            }
        }
    }

    private class DummyBaseDomainMapper : IBaseTrackDomainMapper<Any> {

        override fun mapTrack(track: Any): BaseTrack {
            throw UnsupportedOperationException()
        }

    }

}
