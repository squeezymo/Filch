package me.squeezymo.migrator.deezer.impl.domain.mapper

import me.squeezymo.core.domain.data.BasePlaylist
import me.squeezymo.core.domain.data.EntityWithExternalIDs
import me.squeezymo.core.domain.data.ID
import me.squeezymo.core.domain.data.StreamingServiceID
import me.squeezymo.migrator.api.domain.mapper.IBasePlaylistDomainMapper
import me.squeezymo.migrator.api.domain.mapper.IBaseTrackDomainMapper
import me.squeezymo.migrator.deezer.api.domain.data.DeezerPlaylist
import me.squeezymo.migrator.deezer.api.domain.data.DeezerTrack
import javax.inject.Inject

internal class DeezerBasePlaylistDomainMapper @Inject constructor(
    private val trackMapper: IBaseTrackDomainMapper<DeezerTrack>
) : IBasePlaylistDomainMapper<DeezerTrack, DeezerPlaylist> {

    override suspend fun mapPlaylist(
        playlist: DeezerPlaylist,
        excludeExternalTrackIds: Boolean,
        extractTrackIds: suspend (
            DeezerTrack,
            excludeExternalIds: Boolean
        ) -> Map<StreamingServiceID, ID>
    ): BasePlaylist {
        return BasePlaylist(
            id = playlist.id,
            title = playlist.title,
            tracks = playlist.tracks.map { track ->
                EntityWithExternalIDs(
                    trackMapper.mapTrack(track),
                    extractTrackIds(track, excludeExternalTrackIds)
                )
            },
            thumbnailUrl = playlist.pictureSmall ?: playlist.pictureMedium ?: playlist.pictureBig
        )
    }

}
