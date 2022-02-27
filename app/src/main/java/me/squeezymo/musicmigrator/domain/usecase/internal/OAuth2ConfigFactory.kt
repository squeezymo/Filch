package me.squeezymo.musicmigrator.domain.usecase.internal

import me.squeezymo.migrator.deezer.api.DeezerConst
import me.squeezymo.migrator.spotify.api.SpotifyConst
import me.squeezymo.migrator.youtube.api.YouTubeConst
import me.squeezymo.oauth.api.OAuth2Config
import me.squeezymo.streamingservices.api.domain.data.StreamingService
import javax.inject.Inject

interface IOAuth2ConfigFactory {

    fun getOAuth2ConfigByStreamingService(
        streamingService: StreamingService
    ) : OAuth2Config?

}

internal class OAuth2ConfigFactory @Inject constructor(): IOAuth2ConfigFactory {

    override fun getOAuth2ConfigByStreamingService(
        streamingService: StreamingService
    ): OAuth2Config? {
        if (!streamingService.isEnabled) {
            return null
        }

        return when (streamingService) {
            StreamingService.VK -> null
            StreamingService.APPLE_MUSIC -> null
            StreamingService.YANDEX_MUSIC -> null
            StreamingService.DEEZER -> DeezerConst.oauth2Config
            StreamingService.SPOTIFY -> SpotifyConst.oauth2Config
            StreamingService.YOUTUBE -> YouTubeConst.oauth2Config
        }
    }

}
