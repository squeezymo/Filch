package me.squeezymo.migrator.spotify.impl.network

import dagger.assisted.AssistedFactory
import me.squeezymo.migrator.spotify.impl.domain.cache.SpotifyProfileCache

@AssistedFactory
internal interface SpotifyProfileCacheFactory {

    fun create(
        spotifyDataSource: SpotifyDataSource
    ): SpotifyProfileCache

}
