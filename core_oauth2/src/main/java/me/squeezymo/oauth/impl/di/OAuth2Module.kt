package me.squeezymo.oauth.impl.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import me.squeezymo.oauth.api.DI_DEFAULT_OAUTH2_SERVICE_FACTORY
import me.squeezymo.oauth.api.IOAuth2ServiceFactory
import me.squeezymo.oauth.impl.OAuth2ServiceFactory
import javax.inject.Named

@Module
@InstallIn(SingletonComponent::class)
internal interface OAuth2Module {

    @Binds
    @Named(DI_DEFAULT_OAUTH2_SERVICE_FACTORY)
    fun oAuth2ServiceFactory(
        factory: OAuth2ServiceFactory
    ): IOAuth2ServiceFactory

}
