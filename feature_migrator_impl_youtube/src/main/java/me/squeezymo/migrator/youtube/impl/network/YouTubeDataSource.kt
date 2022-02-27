package me.squeezymo.migrator.youtube.impl.network

import com.google.gson.GsonBuilder
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import me.squeezymo.core.network.BaseDataSource
import me.squeezymo.core.network.interceptor.ModifyHeadersInterceptor
import me.squeezymo.migrator.youtube.impl.DI_GOOGLE_API_KEY
import me.squeezymo.migrator.youtube.impl.network.query.PlaylistItemsResponse
import me.squeezymo.migrator.youtube.impl.network.query.PlaylistsResponse
import me.squeezymo.oauth.api.IOAuth2StateProvider
import me.squeezymo.oauth.api.exception.NoAccessTokenException
import okhttp3.OkHttpClient
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Query
import javax.inject.Named

// https://developers.google.com/youtube/v3/guides/implementation
internal class YouTubeDataSource @AssistedInject constructor(
    httpClientBuilder: OkHttpClient.Builder,
    gsonBuilder: GsonBuilder,
    @Named(DI_GOOGLE_API_KEY) private val apiKey: String,
    @Assisted private val oauth2StateProvider: IOAuth2StateProvider
) : BaseDataSource(httpClientBuilder, gsonBuilder) {

    companion object {

        private const val X_ORIGIN = "https://music.youtube.com"

    }

    private interface Api {

        @GET("playlists?mine=true&part=id,snippet")
        suspend fun playlists(
            @Header("Authorization") token: String,
            @Query("key") apiKey: String,
        ): Response<PlaylistsResponse>

        @GET("playlistItems?part=id,snippet")
        suspend fun playlistItems(
            @Header("Authorization") token: String,
            @Query("key") apiKey: String,
            @Query("playlistId") playlistId: String
        ): Response<PlaylistItemsResponse>

    }

    private val apiHandle = createApiHandle(
        "https://music.youtube.com/youtubei/v1/",
        Api::class,
        buildHttpClient = { builder ->
            builder
                .addInterceptor(
                    ModifyHeadersInterceptor {
                        this
                            .add("x-origin", X_ORIGIN)
                            .add(
                                "User-Agent",
                                "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_4) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/13.1 Safari/605.1.15"
                            )
                            .add("Accept", "*/*")
                            .add("Accept-Language", "en-US,en;q=0.5")
                            .add("Content-Type", "application/json")
                            .add("X-Goog-AuthUser", "0")
                            .add("X-Goog-Visitor-Id", "CgtlWWRoQ0tMMGRqayis26v8BQ")
                    }
                )
                .build()
        }
    )

    suspend fun getPlaylists(): Response<PlaylistsResponse> {
        return apiHandle.playlists(getAuthorizationHeader(), apiKey)
    }

    suspend fun getPlaylistItems(playlistId: String): Response<PlaylistItemsResponse> {
        return apiHandle.playlistItems(getAuthorizationHeader(), apiKey, playlistId)
    }


    private suspend fun getAuthorizationHeader(): String {
        val accessToken = oauth2StateProvider.getFreshAccessState()?.accessToken
            ?: throw NoAccessTokenException(oauth2StateProvider.serviceId)

        return "Bearer $accessToken"
    }

}
