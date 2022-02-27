package me.squeezymo.usercontent.impl.domain.usecase

import me.squeezymo.core.domain.data.BasePlaylist
import me.squeezymo.core.domain.data.ID
import me.squeezymo.core.domain.data.PlaylistTitle
import me.squeezymo.core.domain.data.StreamingServiceID
import me.squeezymo.migrator.deezer.api.domain.IDeezerRepository
import me.squeezymo.migrator.spotify.api.domain.ISpotifyRepository
import me.squeezymo.migrator.youtube.api.domain.IYouTubeRepository
import me.squeezymo.streamingservices.api.domain.data.StreamingService
import me.squeezymo.usercontent.api.domain.usecase.ICreatePlaylistUC
import javax.inject.Inject

internal class CreatePlaylistUC @Inject constructor(
    private val youTubeRepo: IYouTubeRepository,
    private val spotifyRepo: ISpotifyRepository,
    private val deezerRepo: IDeezerRepository
) : ICreatePlaylistUC {

    private val workers = StreamingService.values().associate { service ->
        service to when (service) {
            StreamingService.VK ->
                Worker { TODO() }
            StreamingService.APPLE_MUSIC ->
                Worker { TODO() }
            StreamingService.YANDEX_MUSIC ->
                Worker { TODO() }
            StreamingService.SPOTIFY ->
                Worker(spotifyRepo::createPlaylist)
            StreamingService.DEEZER ->
                Worker(deezerRepo::createPlaylist)
            StreamingService.YOUTUBE ->
                Worker { TODO() }
        }
    }

    override suspend fun createPlaylist(
        serviceId: StreamingServiceID,
        playlistTitle: PlaylistTitle
    ): BasePlaylist {
        return requireNotNull(workers[StreamingService.requireById(serviceId)])
            .createPlaylist(playlistTitle)
            .let { playlistId ->
                BasePlaylist(
                    id = playlistId,
                    title = playlistTitle,
                    tracks = emptyList(),
                    thumbnailUrl = null
                )
            }
    }

    private class Worker(
        val createPlaylist: suspend (
            title: PlaylistTitle
        ) -> ID
    )

}
