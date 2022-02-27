package me.squeezymo.migrator.spotify.impl.network

import dagger.assisted.AssistedFactory
import me.squeezymo.oauth.api.IOAuth2StateProvider

@AssistedFactory
internal interface SpotifyDataSourceFactory {

    fun create(
        oauth2StateProvider: IOAuth2StateProvider
    ): SpotifyDataSource

}
