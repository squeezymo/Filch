package me.squeezymo.migrator.youtube.impl.di

import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import me.squeezymo.migrator.api.domain.mapper.IBasePlaylistDomainMapper
import me.squeezymo.migrator.api.domain.mapper.IBaseTrackDomainMapper
import me.squeezymo.migrator.youtube.BuildConfig
import me.squeezymo.migrator.youtube.api.domain.IYouTubeRepository
import me.squeezymo.migrator.youtube.api.domain.data.YoutubePlaylist
import me.squeezymo.migrator.youtube.api.domain.data.YoutubeTrack
import me.squeezymo.migrator.youtube.impl.DI_GOOGLE_API_KEY
import me.squeezymo.migrator.youtube.impl.domain.YouTubeRepository
import me.squeezymo.migrator.youtube.impl.domain.mapper.YoutubeBasePlaylistDomainMapper
import me.squeezymo.migrator.youtube.impl.domain.mapper.YoutubeBaseTrackDomainMapper
import javax.inject.Named

@Module
@InstallIn(SingletonComponent::class)
internal interface YouTubeModule {

    @Binds
    fun youTubeRepository(
        impl: YouTubeRepository
    ): IYouTubeRepository

    @Binds
    fun youtubeBaseTrackDomainMapper(
        impl: YoutubeBaseTrackDomainMapper
    ): IBaseTrackDomainMapper<YoutubeTrack>

    @Binds
    fun youtubeBasePlaylistDomainMapper(
        impl: YoutubeBasePlaylistDomainMapper
    ): IBasePlaylistDomainMapper<YoutubeTrack, YoutubePlaylist>

    companion object {

        @Provides
        @Named(DI_GOOGLE_API_KEY)
        fun apiKey(): String {
            return BuildConfig.GOOGLE_API_KEY
        }

    }

}
