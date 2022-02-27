package me.squeezymo.usercontent.impl.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import me.squeezymo.usercontent.impl.domain.cache.IPlaylistCache
import me.squeezymo.usercontent.impl.domain.cache.PlaylistCache
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
internal interface CacheModule {

    @Binds
    @Singleton
    fun playlistCache(
        impl: PlaylistCache
    ): IPlaylistCache

}
