package me.squeezymo.musicmigrator.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import me.squeezymo.musicmigrator.domain.OAuth2ServiceFactory
import me.squeezymo.oauth.api.IOAuth2ServiceFactory

@Module
@InstallIn(SingletonComponent::class)
internal interface OAuth2Module {

    @Binds
    fun oAuth2ServiceFactory(
        factory: OAuth2ServiceFactory
    ): IOAuth2ServiceFactory

}
