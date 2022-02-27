package me.squeezymo.musicmigrator.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import me.squeezymo.musicmigrator.domain.OAuth2Broker
import me.squeezymo.musicmigrator.domain.usecase.internal.IOAuth2ConfigFactory
import me.squeezymo.musicmigrator.domain.usecase.internal.OAuth2ConfigFactory
import me.squeezymo.oauth.api.IOAuth2Broker
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
internal interface DomainModule {

    @Binds
    @Singleton
    fun oAuth2Broker(
        broker: OAuth2Broker
    ): IOAuth2Broker

    @Binds
    fun oAuth2ConfigFactory(
        factory: OAuth2ConfigFactory
    ): IOAuth2ConfigFactory

}
