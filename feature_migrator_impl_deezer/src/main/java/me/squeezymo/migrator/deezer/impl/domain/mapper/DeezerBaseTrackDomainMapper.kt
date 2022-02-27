package me.squeezymo.migrator.deezer.impl.domain.mapper

import me.squeezymo.core.domain.data.BaseTrack
import me.squeezymo.migrator.api.domain.mapper.IBaseTrackDomainMapper
import me.squeezymo.migrator.deezer.api.domain.data.DeezerTrack
import java.util.concurrent.TimeUnit
import javax.inject.Inject

internal class DeezerBaseTrackDomainMapper @Inject constructor(

) : IBaseTrackDomainMapper<DeezerTrack> {

    override fun mapTrack(track: DeezerTrack): BaseTrack {
        return BaseTrack(
            title = track.title,
            artist = track.artist?.name,
            album = track.album?.title,
            durationMillis = track.durationSeconds?.toLong()?.let(TimeUnit.SECONDS::toMillis),
            audioPreviewUrl = track.previewUrl,
            thumbnailUrl = track.album?.imageUrl
        )
    }

}
