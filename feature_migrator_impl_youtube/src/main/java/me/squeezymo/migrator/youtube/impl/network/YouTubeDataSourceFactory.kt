package me.squeezymo.migrator.youtube.impl.network

import dagger.assisted.AssistedFactory
import me.squeezymo.oauth.api.IOAuth2StateProvider

@AssistedFactory
internal interface YouTubeDataSourceFactory {

    fun create(
        oauth2StateProvider: IOAuth2StateProvider
    ): YouTubeDataSource

}
