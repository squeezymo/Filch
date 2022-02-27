package me.squeezymo.migrator.spotify.impl.network

import com.google.gson.FieldNamingPolicy
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import me.squeezymo.core.domain.data.PlaylistTitle
import me.squeezymo.core.network.BaseDataSource
import me.squeezymo.core.network.interceptor.ModifyHeadersInterceptor
import me.squeezymo.migrator.spotify.api.SpotifyPlaylistID
import me.squeezymo.migrator.spotify.api.SpotifyTrackID
import me.squeezymo.migrator.spotify.api.SpotifyUserID
import me.squeezymo.migrator.spotify.impl.domain.data.SearchTarget
import me.squeezymo.migrator.spotify.impl.network.dto.SpotifyPlaylistDTO
import me.squeezymo.migrator.spotify.impl.network.dto.SpotifyUserProfileDTO
import me.squeezymo.migrator.spotify.impl.network.query.*
import me.squeezymo.oauth.api.IOAuth2StateProvider
import me.squeezymo.oauth.api.exception.NoAccessTokenException
import okhttp3.OkHttpClient
import retrofit2.Response
import retrofit2.http.*

internal class SpotifyDataSource @AssistedInject constructor(
    httpClientBuilder: OkHttpClient.Builder,
    gsonBuilder: GsonBuilder,
    @Assisted private val oauth2StateProvider: IOAuth2StateProvider
) : BaseDataSource(httpClientBuilder, gsonBuilder) {

    companion object {

        const val MAX_TRACKS_TO_SAVE_PER_REQUEST = 50
        const val MAX_TRACKS_TO_ADD_TO_PLAYLIST_PER_REQUEST = 100

    }

    private interface Api {

        @GET("me")
        suspend fun getProfile(
            @Header("Authorization") token: String
        ): Response<SpotifyUserProfileDTO>

        @GET("me/playlists")
        suspend fun getPlaylists(
            @Header("Authorization") token: String,
            @Query("limit") limit: Int = 50,
            @Query("offset") offset: Int = 0
        ): Response<SpotifyPlaylistsResponse>

        @GET
        suspend fun getPlaylistsByUrl(
            @Header("Authorization") token: String,
            @Url url: String
        ): Response<SpotifyPlaylistsResponse>

        @GET("me/tracks")
        suspend fun getTracks(
            @Header("Authorization") token: String,
            @Query("limit") limit: Int = 50,
            @Query("offset") offset: Int = 0
        ): Response<SpotifyTracksResponse>

        @GET("playlists/{playlist_id}/tracks")
        suspend fun getTracksByPlaylist(
            @Header("Authorization") token: String,
            @Path("playlist_id") playlistId: SpotifyPlaylistID
        ): Response<SpotifyTracksResponse>

        @POST("users/{user_id}/playlists")
        suspend fun createPlaylist(
            @Header("Authorization") token: String,
            @Path("user_id") userId: String,
            @Body body: SpotifyCreatePlaylistBody
        ): Response<SpotifyPlaylistDTO>

        @POST("playlists/{playlist_id}/tracks")
        suspend fun addTracksToPlaylist(
            @Header("Authorization") token: String,
            @Path("playlist_id") playlistId: SpotifyPlaylistID,
            @Body body: SpotifyAddToPlaylistBody
        ): Response<SpotifyAddToPlaylistResponse>

        @GET
        suspend fun getTracksByUrl(
            @Header("Authorization") token: String,
            @Url url: String
        ): Response<SpotifyTracksResponse>

        @PUT("me/tracks")
        suspend fun saveTracks(
            @Header("Authorization") token: String,
            @Query("ids") trackIds: String
        ): Response<List<SpotifyTrackID>>

        @GET("search")
        suspend fun search(
            @Header("Authorization") token: String,
            @Query("type") searchTarget: String,
            @Query("q") searchQuery: String,
            @Query("limit") limit: Int = 5,
            @Query("offset") offset: Int = 0
        ): Response<SpotifySearchResponse>

    }

    private val apiHandle = createApiHandle(
        "https://api.spotify.com/v1/",
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

    suspend fun getUserProfile(): Response<SpotifyUserProfileDTO> {
        return apiHandle.getProfile(getAuthorizationHeader())
    }

    suspend fun getPlaylists(): Response<SpotifyPlaylistsResponse> {
        return apiHandle.getPlaylists(getAuthorizationHeader())
    }

    suspend fun getPlaylistsByUrl(url: String): Response<SpotifyPlaylistsResponse> {
        return apiHandle.getPlaylistsByUrl(getAuthorizationHeader(), url)
    }

    suspend fun getTracks(): Response<SpotifyTracksResponse> {
        return apiHandle.getTracks(getAuthorizationHeader())
    }

    suspend fun getTracks(playlistId: SpotifyPlaylistID): Response<SpotifyTracksResponse> {
        return apiHandle.getTracksByPlaylist(getAuthorizationHeader(), playlistId)
    }

    suspend fun saveTracks(trackIds: List<SpotifyTrackID>): Response<List<SpotifyTrackID>> {
        check(trackIds.size <= MAX_TRACKS_TO_SAVE_PER_REQUEST) {
            "No more than $MAX_TRACKS_TO_SAVE_PER_REQUEST tracks can be saved per single request"
        }

        return apiHandle.saveTracks(getAuthorizationHeader(), trackIds.joinToString(","))
    }

    suspend fun createPlaylist(
        userId: SpotifyUserID,
        playlistTitle: PlaylistTitle,
        playlistDescription: String = "",
        isPublic: Boolean = true,
        isCollaborative: Boolean = false
    ): Response<SpotifyPlaylistDTO> {
        return apiHandle.createPlaylist(
            getAuthorizationHeader(),
            userId,
            SpotifyCreatePlaylistBody(
                name = playlistTitle,
                public = isPublic,
                collaborative = isCollaborative,
                description = playlistDescription
            )
        )
    }

    suspend fun saveTracksToPlaylist(
        playlistId: SpotifyPlaylistID,
        trackIds: List<SpotifyTrackID>
    ): Response<SpotifyAddToPlaylistResponse> {
        return apiHandle.addTracksToPlaylist(
            getAuthorizationHeader(),
            playlistId,
            SpotifyAddToPlaylistBody(
                trackIds.map { trackId -> "spotify:track:$trackId" }
            )
        )
    }

    suspend fun getTracksByUrl(url: String): Response<SpotifyTracksResponse> {
        return apiHandle.getTracksByUrl(getAuthorizationHeader(), url)
    }

    suspend fun search(
        targets: List<SearchTarget>,
        track: String?,
        artist: String?,
        album: String?,
        year: Int?
    ): Response<SpotifySearchResponse> {
        check(targets.isNotEmpty()) {
            "Expecting at least one search target"
        }

        val searchQueryBuilder = StringBuilder()

        if (track != null) {
            if (searchQueryBuilder.isNotEmpty()) searchQueryBuilder.append(" ")
            searchQueryBuilder.append("track:$track")
        }

        if (artist != null) {
            if (searchQueryBuilder.isNotEmpty()) searchQueryBuilder.append(" ")
            searchQueryBuilder.append("artist:$artist")
        }

        if (album != null) {
            if (searchQueryBuilder.isNotEmpty()) searchQueryBuilder.append(" ")
            searchQueryBuilder.append("album:$album")
        }

        if (year != null) {
            if (searchQueryBuilder.isNotEmpty()) searchQueryBuilder.append(" ")
            searchQueryBuilder.append("year:$year")
        }

        return apiHandle.search(
            getAuthorizationHeader(),
            targets.joinToString(",") { it.value },
            searchQueryBuilder.toString()
        )
    }

    private suspend fun getAuthorizationHeader(): String {
        val accessToken = oauth2StateProvider.getFreshAccessState()?.accessToken
            ?: throw NoAccessTokenException(oauth2StateProvider.serviceId)

        return "Bearer $accessToken"
    }

}
