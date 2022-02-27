package me.squeezymo.migrator.spotify.impl.domain.cache

import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import me.squeezymo.core.domain.data.StreamingServiceID
import me.squeezymo.migrator.spotify.impl.network.SpotifyDataSource
import me.squeezymo.migrator.spotify.impl.network.dto.SpotifyUserProfileDTO
import me.squeezymo.oauth.api.AccessToken
import me.squeezymo.oauth.api.exception.NoAccessTokenException
import okhttp3.ResponseBody

internal class SpotifyProfileCache @AssistedInject constructor(
    @Assisted private val spotifyDataSource: SpotifyDataSource
) {

    private var lastKnownAccessToken: AccessToken? = null
    private var lastKnownProfile: SpotifyUserProfileDTO? = null

    suspend fun getProfile(
        spotifyServiceId: StreamingServiceID,
        accessToken: AccessToken?,
        handleError: (errorBody: ResponseBody?) -> Nothing
    ): SpotifyUserProfileDTO {
        if (accessToken == null) {
            throw NoAccessTokenException(spotifyServiceId)
        }

        val currentToken = lastKnownAccessToken
        val profile = lastKnownProfile

        if (currentToken == accessToken && profile != null) {
            return profile
        }

        val response = spotifyDataSource.getUserProfile()

        if (response.isSuccessful) {
            return response.body()!!
        }

        handleError(response.errorBody()!!)
    }

}
