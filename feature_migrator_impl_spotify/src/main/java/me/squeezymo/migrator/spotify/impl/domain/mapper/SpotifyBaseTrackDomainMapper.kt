package me.squeezymo.migrator.spotify.impl.domain.mapper

import me.squeezymo.core.domain.data.BaseTrack
import me.squeezymo.migrator.api.domain.mapper.IBaseTrackDomainMapper
import me.squeezymo.migrator.spotify.api.domain.data.SpotifyTrack
import javax.inject.Inject

internal class SpotifyBaseTrackDomainMapper @Inject constructor(

) : IBaseTrackDomainMapper<SpotifyTrack> {

    override fun mapTrack(track: SpotifyTrack): BaseTrack {
        return BaseTrack(
            title = track.name,
            artist = track.artists.firstOrNull()?.name,
            album = track.album?.name,
            durationMillis = track.durationMillis,
            audioPreviewUrl = track.previewUrl,
            thumbnailUrl = track.album?.imageUrl
        )
    }

}
