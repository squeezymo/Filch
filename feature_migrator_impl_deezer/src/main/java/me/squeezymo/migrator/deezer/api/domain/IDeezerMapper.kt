package me.squeezymo.migrator.deezer.api.domain

import me.squeezymo.core.domain.data.BaseTrack
import me.squeezymo.migrator.deezer.api.domain.data.DeezerTrack

interface IDeezerMapper {

    fun mapTrack(track: DeezerTrack): BaseTrack

}
