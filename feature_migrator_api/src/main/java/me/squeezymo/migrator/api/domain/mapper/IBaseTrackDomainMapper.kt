package me.squeezymo.migrator.api.domain.mapper

import me.squeezymo.core.domain.data.BaseTrack

interface IBaseTrackDomainMapper<T> {

    fun mapTrack(track: T): BaseTrack

}
