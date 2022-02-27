package me.squeezymo.migrator.deezer.impl.network

import dagger.assisted.AssistedFactory
import me.squeezymo.oauth.api.IOAuth2StateProvider

@AssistedFactory
internal interface DeezerDataSourceFactory {

    fun create(
        oauth2StateProvider: IOAuth2StateProvider
    ): DeezerDataSource

}
