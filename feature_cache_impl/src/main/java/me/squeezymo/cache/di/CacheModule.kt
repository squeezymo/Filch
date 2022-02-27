package me.squeezymo.cache.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import me.squeezymo.cache.api.ICacheDataSource
import me.squeezymo.cache.impl.CacheDataSource

@Module
@InstallIn(SingletonComponent::class)
internal interface CacheModule {

    @Binds
    fun cacheDataSource(
        dataSource: CacheDataSource
    ): ICacheDataSource

}
