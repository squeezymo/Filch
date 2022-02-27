package me.squeezymo.usercontent.impl.domain.usecase

import me.squeezymo.core.domain.data.BasePlaylist
import me.squeezymo.core.domain.data.ID
import me.squeezymo.core.domain.data.StreamingServiceID
import me.squeezymo.migrator.api.domain.mapper.IBasePlaylistDomainMapper
import me.squeezymo.migrator.deezer.api.domain.IDeezerRepository
import me.squeezymo.migrator.deezer.api.domain.data.DeezerPlaylist
import me.squeezymo.migrator.deezer.api.domain.data.DeezerTrack
import me.squeezymo.migrator.spotify.api.domain.ISpotifyRepository
import me.squeezymo.migrator.spotify.api.domain.data.SpotifyPlaylist
import me.squeezymo.migrator.spotify.api.domain.data.SpotifyTrack
import me.squeezymo.migrator.youtube.api.domain.IYouTubeRepository
import me.squeezymo.migrator.youtube.api.domain.data.YoutubePlaylist
import me.squeezymo.migrator.youtube.api.domain.data.YoutubeTrack
import me.squeezymo.streamingservices.api.domain.data.StreamingService
import me.squeezymo.usercontent.api.domain.usecase.IGetPlaylistsUC
import javax.inject.Inject

internal class GetPlaylistsUC @Inject constructor(
    private val youTubeRepo: IYouTubeRepository,
    private val youtubeBaseMapper: IBasePlaylistDomainMapper<YoutubeTrack, YoutubePlaylist>,
    private val spotifyRepo: ISpotifyRepository,
    private val spotifyBaseMapper: IBasePlaylistDomainMapper<SpotifyTrack, SpotifyPlaylist>,
    private val deezerRepo: IDeezerRepository,
    private val deezerBaseMapper: IBasePlaylistDomainMapper<DeezerTrack, DeezerPlaylist>
) : IGetPlaylistsUC {

    private val workers = StreamingService.values().associate { service ->
        service to when (service) {
            StreamingService.VK ->
                Worker({ TODO() }, { _, _ -> TODO() }, DummyBaseDomainMapper())
            StreamingService.APPLE_MUSIC ->
                Worker({ TODO() }, { _, _ -> TODO() }, DummyBaseDomainMapper())
            StreamingService.YANDEX_MUSIC ->
                Worker({ TODO() }, { _, _ -> TODO() }, DummyBaseDomainMapper())
            StreamingService.SPOTIFY ->
                Worker(
                    spotifyRepo::requestPlaylists,
                    spotifyRepo::extractTrackIds,
                    spotifyBaseMapper
                )
            StreamingService.DEEZER ->
                Worker(
                    deezerRepo::requestPlaylists,
                    deezerRepo::extractTrackIds,
                    deezerBaseMapper
                )
            StreamingService.YOUTUBE ->
                Worker(
                    youTubeRepo::requestPlaylists,
                    { _, _ -> TODO() },
                    youtubeBaseMapper
                )
        }
    }

    override suspend fun getPlaylists(
        serviceId: StreamingServiceID,
        excludeExternalTrackIds: Boolean
    ): List<BasePlaylist> {
        return requireNotNull(workers[StreamingService.requireById(serviceId)])
            .requestPlaylists(excludeExternalTrackIds)
    }

    private class Worker<T, P>(
        private val request: suspend () -> List<P>,
        private val extractTrackIds: suspend (
            track: T,
            excludeExternalTrackIds: Boolean
        ) -> Map<StreamingServiceID, ID>,
        private val mapper: IBasePlaylistDomainMapper<T, P>
    ) {
        suspend fun requestPlaylists(
            excludeExternalTrackIds: Boolean
        ): List<BasePlaylist> {
            return request().map { playlist ->
                mapper.mapPlaylist(playlist, excludeExternalTrackIds, extractTrackIds)
            }
        }
    }

    private class DummyBaseDomainMapper : IBasePlaylistDomainMapper<Any, Any> {

        override suspend fun mapPlaylist(
            playlist: Any,
            excludeExternalTrackIds: Boolean,
            extractTrackIds: suspend (Any, Boolean) -> Map<StreamingServiceID, ID>
        ): BasePlaylist {
            throw UnsupportedOperationException()
        }

    }

}
