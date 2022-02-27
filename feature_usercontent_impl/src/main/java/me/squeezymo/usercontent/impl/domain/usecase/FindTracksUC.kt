package me.squeezymo.usercontent.impl.domain.usecase

import me.squeezymo.core.domain.data.BaseTrack
import me.squeezymo.core.domain.data.EntityWithExternalIDs
import me.squeezymo.core.domain.data.ID
import me.squeezymo.core.domain.data.StreamingServiceID
import me.squeezymo.migrator.api.domain.mapper.IBaseTrackDomainMapper
import me.squeezymo.migrator.deezer.api.domain.IDeezerRepository
import me.squeezymo.migrator.deezer.api.domain.data.DeezerTrack
import me.squeezymo.migrator.spotify.api.domain.ISpotifyRepository
import me.squeezymo.migrator.spotify.api.domain.data.SpotifyTrack
import me.squeezymo.migrator.youtube.api.domain.IYouTubeRepository
import me.squeezymo.migrator.youtube.api.domain.data.YoutubeTrack
import me.squeezymo.streamingservices.api.domain.data.StreamingService
import me.squeezymo.usercontent.api.domain.usecase.IFindTracksUC
import javax.inject.Inject

internal class FindTracksUC @Inject constructor(
    private val youTubeRepo: IYouTubeRepository,
    private val youtubeBaseMapper: IBaseTrackDomainMapper<YoutubeTrack>,
    private val spotifyRepo: ISpotifyRepository,
    private val spotifyBaseMapper: IBaseTrackDomainMapper<SpotifyTrack>,
    private val deezerRepo: IDeezerRepository,
    private val deezerBaseMapper: IBaseTrackDomainMapper<DeezerTrack>
) : IFindTracksUC {

    private val workers = StreamingService.values().associate { service ->
        service to when (service) {
            StreamingService.VK ->
                Worker({ _, _, _, _ -> TODO() }, DummyBaseDomainMapper())
            StreamingService.APPLE_MUSIC ->
                Worker({ _, _, _, _ -> TODO() }, DummyBaseDomainMapper())
            StreamingService.YANDEX_MUSIC ->
                Worker({ _, _, _, _ -> TODO() }, DummyBaseDomainMapper())
            StreamingService.SPOTIFY ->
                Worker({ track, artist, album, externalIds ->
                    spotifyRepo.findTrack(track, artist, album, externalIds = externalIds)
                }, spotifyBaseMapper)
            StreamingService.DEEZER ->
                Worker(deezerRepo::findTrack, deezerBaseMapper)
            StreamingService.YOUTUBE ->
                Worker({ _, _, _, _ -> TODO() }, DummyBaseDomainMapper())
        }
    }

    override suspend fun findTrack(
        serviceId: StreamingServiceID,
        track: String,
        artist: String?,
        album: String?,
        externalIds: Map<StreamingServiceID, ID>
    ): EntityWithExternalIDs<BaseTrack>? {
        return requireNotNull(workers[StreamingService.requireById(serviceId)])
            .findTrack(track, artist, album, externalIds)
    }

    private class Worker<T>(
        private val request: suspend (
            track: String,
            artist: String?,
            album: String?,
            externalIds: Map<StreamingServiceID, ID>
        ) -> EntityWithExternalIDs<T>?,
        private val mapper: IBaseTrackDomainMapper<T>
    ) {
        suspend fun findTrack(
            track: String,
            artist: String?,
            album: String?,
            externalIds: Map<StreamingServiceID, ID>
        ): EntityWithExternalIDs<BaseTrack>? {
            return request(track, artist, album, externalIds)?.map(mapper::mapTrack)
        }
    }

    private class DummyBaseDomainMapper : IBaseTrackDomainMapper<Any> {

        override fun mapTrack(track: Any): BaseTrack {
            throw UnsupportedOperationException()
        }

    }

}
