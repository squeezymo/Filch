package me.squeezymo.streamingservices.impl.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import dagger.hilt.android.scopes.ViewModelScoped
import me.squeezymo.streamingservices.api.domain.usecase.IOAuth2UC
import me.squeezymo.streamingservices.impl.domain.usecase.OAuth2UC

@Module
@InstallIn(ViewModelComponent::class)
internal interface StreamingServicesModule {

    @Binds
    @ViewModelScoped
    fun oAuth2Uc(
        impl: OAuth2UC
    ): IOAuth2UC

}
