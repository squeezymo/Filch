package me.squeezymo.migrator.deezer.impl.domain

import me.squeezymo.core.domain.data.BaseTrack
import me.squeezymo.migrator.deezer.api.domain.IDeezerMapper
import me.squeezymo.migrator.deezer.api.domain.data.DeezerTrack
import javax.inject.Inject

internal class DeezerMapper @Inject constructor(): IDeezerMapper {
    override fun mapTrack(track: DeezerTrack): BaseTrack {
        TODO("Not yet implemented")
    }


}
