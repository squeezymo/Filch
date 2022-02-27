package me.squeezymo.migrator.youtube.impl.domain.mapper

import me.squeezymo.core.domain.data.BasePlaylist
import me.squeezymo.core.domain.data.ID
import me.squeezymo.core.domain.data.StreamingServiceID
import me.squeezymo.migrator.api.domain.mapper.IBasePlaylistDomainMapper
import me.squeezymo.migrator.api.domain.mapper.IBaseTrackDomainMapper
import me.squeezymo.migrator.youtube.api.domain.data.YoutubePlaylist
import me.squeezymo.migrator.youtube.api.domain.data.YoutubeTrack
import javax.inject.Inject

internal class YoutubeBasePlaylistDomainMapper @Inject constructor(
    private val trackMapper: IBaseTrackDomainMapper<YoutubeTrack>
) : IBasePlaylistDomainMapper<YoutubeTrack, YoutubePlaylist> {

    override suspend fun mapPlaylist(
        playlist: YoutubePlaylist,
        excludeExternalTrackIds: Boolean,
        extractTrackIds: suspend (
            track: YoutubeTrack,
            excludeExternalIds: Boolean
        ) -> Map<StreamingServiceID, ID>
    ): BasePlaylist {
        TODO("Not yet implemented")
    }

}
