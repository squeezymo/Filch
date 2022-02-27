package me.squeezymo.migrator.deezer.impl.network

import com.google.gson.FieldNamingPolicy
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import me.squeezymo.core.network.BaseDataSource
import me.squeezymo.core.network.interceptor.ModifyHeadersInterceptor
import me.squeezymo.migrator.deezer.api.DeezerPlaylistID
import me.squeezymo.migrator.deezer.api.DeezerTrackID
import me.squeezymo.migrator.deezer.impl.network.dto.*
import me.squeezymo.migrator.deezer.impl.network.query.DeezerPaginableData
import me.squeezymo.migrator.deezer.impl.network.query.EmptyBody
import me.squeezymo.oauth.api.IOAuth2StateProvider
import me.squeezymo.oauth.api.exception.NoAccessTokenException
import okhttp3.OkHttpClient
import retrofit2.Response
import retrofit2.http.*

internal class DeezerDataSource @AssistedInject constructor(
    httpClientBuilder: OkHttpClient.Builder,
    gsonBuilder: GsonBuilder,
    @Assisted private val oauth2StateProvider: IOAuth2StateProvider
) : BaseDataSource(httpClientBuilder, gsonBuilder) {

    private interface Api {

        @GET("user/me/albums")
        suspend fun getAlbums(
            @Query("access_token") token: String
        ): Response<DeezerPaginableData<DeezerAlbumDTO>>

        @GET
        suspend fun getAlbumsByUrl(
            @Url url: String,
            @Query("access_token") token: String,
        ): Response<DeezerPaginableData<DeezerAlbumDTO>>

        @GET("user/me/artists")
        suspend fun getArtists(
            @Query("access_token") token: String
        ): Response<DeezerPaginableData<DeezerArtistDTO>>

        @GET
        suspend fun getArtistsByUrl(
            @Url url: String,
            @Query("access_token") token: String,
        ): Response<DeezerPaginableData<DeezerArtistDTO>>

        @GET("user/me/playlists")
        suspend fun getPlaylists(
            @Query("access_token") token: String
        ): Response<DeezerPaginableData<DeezerPlaylistShortDTO>>

        @GET
        suspend fun getPlaylistsByUrl(
            @Url url: String,
            @Query("access_token") token: String,
        ): Response<DeezerPaginableData<DeezerPlaylistShortDTO>>

        @GET("playlist/{playlist_id}")
        suspend fun getPlaylist(
            @Path("playlist_id") playlistId: DeezerPlaylistID,
            @Query("access_token") token: String
        ): Response<DeezerPlaylistFullDTO>

        @GET("user/me/tracks")
        suspend fun getTracks(
            @Query("access_token") token: String
        ): Response<DeezerPaginableData<DeezerTrackDTO>>

        @POST("user/me/tracks")
        suspend fun addTrackToFavorites(
            @Query("access_token") token: String,
            @Query("track_id") id: Long,
            @Body body: EmptyBody = EmptyBody()
        ): Response<String>

        @GET
        suspend fun getTracksByUrl(
            @Url url: String,
            @Query("access_token") token: String
        ): Response<DeezerPaginableData<DeezerTrackDTO>>

        @GET("search/track")
        suspend fun searchTracks(
            @Query("q") searchQuery: String
        ): Response<DeezerPaginableData<DeezerTrackDTO>>

    }

    private val apiHandle = createApiHandle(
        "https://api.deezer.com/",
        Api::class,
        buildHttpClient = { builder ->
            builder
                .addInterceptor(
                    ModifyHeadersInterceptor {
                        add("Accept", "application/json")
                    }
                )
                .build()
        }
    )

    override fun createGson(gsonBuilder: GsonBuilder): Gson {
        return gsonBuilder
            .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
            .create()
    }

    suspend fun getAlbums(): Response<DeezerPaginableData<DeezerAlbumDTO>> {
        return apiHandle.getAlbums(getAccessToken())
    }

    suspend fun getAlbumsByUrl(url: String): Response<DeezerPaginableData<DeezerAlbumDTO>> {
        return apiHandle.getAlbumsByUrl(url, getAccessToken())
    }

    suspend fun getArtists(): Response<DeezerPaginableData<DeezerArtistDTO>> {
        return apiHandle.getArtists(getAccessToken())
    }

    suspend fun getArtistsByUrl(url: String): Response<DeezerPaginableData<DeezerArtistDTO>> {
        return apiHandle.getArtistsByUrl(url, getAccessToken())
    }

    suspend fun getPlaylists(): Response<DeezerPaginableData<DeezerPlaylistShortDTO>> {
        return apiHandle.getPlaylists(getAccessToken())
    }

    suspend fun getPlaylistsByUrl(url: String): Response<DeezerPaginableData<DeezerPlaylistShortDTO>> {
        return apiHandle.getPlaylistsByUrl(url, getAccessToken())
    }

    suspend fun getPlaylist(playlistId: DeezerPlaylistID): Response<DeezerPlaylistFullDTO> {
        return apiHandle.getPlaylist(playlistId, getAccessToken())
    }

    suspend fun getTracks(): Response<DeezerPaginableData<DeezerTrackDTO>> {
        return apiHandle.getTracks(getAccessToken())
    }

    suspend fun addTrackToFavorites(
        trackId: DeezerTrackID
    ): Response<String> {
        return apiHandle.addTrackToFavorites(getAccessToken(), trackId.toLong())
    }

    suspend fun getTracksByUrl(url: String): Response<DeezerPaginableData<DeezerTrackDTO>> {
        return apiHandle.getTracksByUrl(url, getAccessToken())
    }

    suspend fun search(
        track: String?,
        artist: String?,
        album: String?
    ): Response<DeezerPaginableData<DeezerTrackDTO>> {
        val searchQueryBuilder = StringBuilder()

        if (track != null) {
            if (searchQueryBuilder.isNotEmpty()) searchQueryBuilder.append(" ")
            searchQueryBuilder.append("track:\"$track\"")
        }

        if (artist != null) {
            if (searchQueryBuilder.isNotEmpty()) searchQueryBuilder.append(" ")
            searchQueryBuilder.append("artist:\"$artist\"")
        }

        if (album != null) {
            if (searchQueryBuilder.isNotEmpty()) searchQueryBuilder.append(" ")
            searchQueryBuilder.append("album:\"$album\"")
        }

        return apiHandle.searchTracks(
            searchQueryBuilder.toString()
        )
    }

    private suspend fun getAccessToken(): String {
        return oauth2StateProvider.getFreshAccessState()?.accessToken
            ?: throw NoAccessTokenException(oauth2StateProvider.serviceId)
    }

}
