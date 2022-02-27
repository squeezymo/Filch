package me.squeezymo.migrator.deezer.impl.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import me.squeezymo.migrator.deezer.api.DI_DEEZER_OAUTH2_SERVICE_FACTORY
import me.squeezymo.migrator.deezer.impl.oauth.DeezerOAuth2ServiceFactory
import me.squeezymo.oauth.api.IOAuth2ServiceFactory
import javax.inject.Named

@Module
@InstallIn(SingletonComponent::class)
internal interface OAuth2Module {

    @Binds
    @Named(DI_DEEZER_OAUTH2_SERVICE_FACTORY)
    fun oAuth2ServiceFactory(
        factory: DeezerOAuth2ServiceFactory
    ): IOAuth2ServiceFactory

}
