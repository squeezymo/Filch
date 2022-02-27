package me.squeezymo.migrator.youtube.impl.domain.mapper

import me.squeezymo.core.domain.data.BaseTrack
import me.squeezymo.migrator.api.domain.mapper.IBaseTrackDomainMapper
import me.squeezymo.migrator.youtube.api.domain.data.YoutubeTrack
import javax.inject.Inject

internal class YoutubeBaseTrackDomainMapper @Inject constructor(

) : IBaseTrackDomainMapper<YoutubeTrack> {

    override fun mapTrack(track: YoutubeTrack): BaseTrack {
        TODO("Not yet implemented")
    }

}
