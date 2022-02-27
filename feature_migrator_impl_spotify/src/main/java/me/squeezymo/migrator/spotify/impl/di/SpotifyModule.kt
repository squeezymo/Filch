package me.squeezymo.migrator.spotify.impl.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import me.squeezymo.migrator.api.domain.mapper.IBasePlaylistDomainMapper
import me.squeezymo.migrator.api.domain.mapper.IBaseTrackDomainMapper
import me.squeezymo.migrator.spotify.api.domain.ISpotifyRepository
import me.squeezymo.migrator.spotify.api.domain.data.SpotifyPlaylist
import me.squeezymo.migrator.spotify.api.domain.data.SpotifyTrack
import me.squeezymo.migrator.spotify.impl.domain.SpotifyRepository
import me.squeezymo.migrator.spotify.impl.domain.mapper.ISpotifyDtoToDomainMapper
import me.squeezymo.migrator.spotify.impl.domain.mapper.SpotifyBasePlaylistDomainMapper
import me.squeezymo.migrator.spotify.impl.domain.mapper.SpotifyBaseTrackDomainMapper
import me.squeezymo.migrator.spotify.impl.domain.mapper.SpotifyDtoToDomainMapper

@Module
@InstallIn(SingletonComponent::class)
internal interface SpotifyModule {

    @Binds
    fun spotifyDtoToDomainMapper(
        impl: SpotifyDtoToDomainMapper
    ): ISpotifyDtoToDomainMapper

    @Binds
    fun spotifyRepository(
        impl: SpotifyRepository
    ): ISpotifyRepository

    @Binds
    fun spotifyBaseTrackDomainMapper(
        impl: SpotifyBaseTrackDomainMapper
    ): IBaseTrackDomainMapper<SpotifyTrack>

    @Binds
    fun spotifyBasePlaylistDomainMapper(
        impl: SpotifyBasePlaylistDomainMapper
    ): IBasePlaylistDomainMapper<SpotifyTrack, SpotifyPlaylist>

}
