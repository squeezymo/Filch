package me.squeezymo.migrator.deezer.impl.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import me.squeezymo.migrator.api.domain.mapper.IBasePlaylistDomainMapper
import me.squeezymo.migrator.api.domain.mapper.IBaseTrackDomainMapper
import me.squeezymo.migrator.deezer.api.domain.IDeezerRepository
import me.squeezymo.migrator.deezer.api.domain.data.DeezerPlaylist
import me.squeezymo.migrator.deezer.api.domain.data.DeezerTrack
import me.squeezymo.migrator.deezer.impl.domain.DeezerRepository
import me.squeezymo.migrator.deezer.impl.domain.mapper.DeezerBasePlaylistDomainMapper
import me.squeezymo.migrator.deezer.impl.domain.mapper.DeezerBaseTrackDomainMapper
import me.squeezymo.migrator.deezer.impl.domain.mapper.DeezerDtoToDomainMapper
import me.squeezymo.migrator.deezer.impl.domain.mapper.IDeezerDtoToDomainMapper

@Module
@InstallIn(SingletonComponent::class)
internal interface DeezerModule {

    @Binds
    fun deezerDtoToDomainMapper(
        impl: DeezerDtoToDomainMapper
    ): IDeezerDtoToDomainMapper

    @Binds
    fun deezerRepository(
        impl: DeezerRepository
    ): IDeezerRepository

    @Binds
    fun deezerBaseTrackDomainMapper(
        impl: DeezerBaseTrackDomainMapper
    ): IBaseTrackDomainMapper<DeezerTrack>

    @Binds
    fun deezerBasePlaylistDomainMapper(
        impl: DeezerBasePlaylistDomainMapper
    ): IBasePlaylistDomainMapper<DeezerTrack, DeezerPlaylist>

}
