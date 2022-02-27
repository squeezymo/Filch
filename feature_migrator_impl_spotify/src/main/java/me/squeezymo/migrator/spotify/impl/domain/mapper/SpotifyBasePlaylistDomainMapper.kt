package me.squeezymo.migrator.spotify.impl.domain.mapper

import me.squeezymo.core.domain.data.BasePlaylist
import me.squeezymo.core.domain.data.EntityWithExternalIDs
import me.squeezymo.core.domain.data.ID
import me.squeezymo.core.domain.data.StreamingServiceID
import me.squeezymo.migrator.api.domain.mapper.IBasePlaylistDomainMapper
import me.squeezymo.migrator.api.domain.mapper.IBaseTrackDomainMapper
import me.squeezymo.migrator.spotify.api.domain.data.SpotifyPlaylist
import me.squeezymo.migrator.spotify.api.domain.data.SpotifyTrack
import javax.inject.Inject

internal class SpotifyBasePlaylistDomainMapper @Inject constructor(
    private val trackMapper: IBaseTrackDomainMapper<SpotifyTrack>
) : IBasePlaylistDomainMapper<SpotifyTrack, SpotifyPlaylist> {

    override suspend fun mapPlaylist(
        playlist: SpotifyPlaylist,
        excludeExternalTrackIds: Boolean,
        extractTrackIds: suspend (
            SpotifyTrack,
            excludeExternalTrackIds: Boolean
        ) -> Map<StreamingServiceID, ID>
    ): BasePlaylist {
        return BasePlaylist(
            id = playlist.id,
            title = playlist.name,
            tracks = playlist.tracks.map { track ->
                EntityWithExternalIDs(
                    trackMapper.mapTrack(track),
                    extractTrackIds(track, excludeExternalTrackIds)
                )
            },
            thumbnailUrl = playlist.imageUrl
        )
    }

}
