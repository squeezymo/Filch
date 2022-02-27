package me.squeezymo.migrator.api.domain.mapper

import me.squeezymo.core.domain.data.BasePlaylist
import me.squeezymo.core.domain.data.ID
import me.squeezymo.core.domain.data.StreamingServiceID

interface IBasePlaylistDomainMapper<T, P> {

    suspend fun mapPlaylist(
        playlist: P,
        excludeExternalTrackIds: Boolean,
        extractTrackIds: suspend (
            track: T,
            excludeExternalIds: Boolean
        ) -> Map<StreamingServiceID, ID>
    ): BasePlaylist

}
