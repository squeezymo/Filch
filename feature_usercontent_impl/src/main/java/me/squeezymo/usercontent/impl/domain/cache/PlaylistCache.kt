package me.squeezymo.usercontent.impl.domain.cache

import me.squeezymo.core.domain.data.BasePlaylist
import me.squeezymo.core.domain.data.ID
import me.squeezymo.core.domain.data.PlaylistTitle
import me.squeezymo.core.domain.data.StreamingServiceID
import me.squeezymo.streamingservices.api.domain.data.StreamingService
import javax.inject.Inject

internal interface IPlaylistCache {

    fun setPlaylist(
        service: StreamingService,
        playlist: BasePlaylist
    )

    fun getPlaylist(
        service: StreamingService,
        title: PlaylistTitle
    ): BasePlaylist?

    fun setTrackMapping(
        fromService: StreamingService,
        toService: StreamingService,
        srcIdToDstIdTrackMapping: Map<ID, ID>
    )

    fun getSrcIdToDstIdTrackMapping(
        fromService: StreamingService,
        toService: StreamingService
    ): Map<ID, ID>?

    fun clearPlaylist(
        service: StreamingService,
        title: PlaylistTitle
    )

}

internal class PlaylistCache @Inject constructor(

) : IPlaylistCache {

    private val playlistsCache: MutableMap<PlaylistKey, BasePlaylist> = HashMap()
    private val trackMappingCache: MutableMap<TrackMappingKey, Map<ID, ID>> = HashMap()

    override fun setPlaylist(
        service: StreamingService,
        playlist: BasePlaylist
    ) {
        playlistsCache[PlaylistKey(service.id, playlist.title)] = playlist
    }

    override fun getPlaylist(
        service: StreamingService,
        title: PlaylistTitle
    ): BasePlaylist? {
        return playlistsCache[PlaylistKey(service.id, title)]
    }

    override fun setTrackMapping(
        fromService: StreamingService,
        toService: StreamingService,
        srcIdToDstIdTrackMapping: Map<ID, ID>
    ) {
        trackMappingCache[TrackMappingKey(fromService, toService)] = srcIdToDstIdTrackMapping
    }

    override fun getSrcIdToDstIdTrackMapping(
        fromService: StreamingService,
        toService: StreamingService
    ): Map<ID, ID>? {
        return trackMappingCache[TrackMappingKey(fromService, toService)]
    }

    override fun clearPlaylist(
        service: StreamingService,
        title: PlaylistTitle
    ) {
        playlistsCache.remove(PlaylistKey(service.id, title))
    }

    private data class PlaylistKey(
        val serviceID: StreamingServiceID,
        val title: PlaylistTitle
    )

    private data class TrackMappingKey(
        val fromService: StreamingService,
        val toService: StreamingService
    )

}
