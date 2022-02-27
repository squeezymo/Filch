package me.squeezymo.migrator.deezer.api.domain

import kotlinx.coroutines.flow.Flow
import me.squeezymo.core.domain.data.*
import me.squeezymo.migrator.deezer.api.DeezerPlaylistID
import me.squeezymo.migrator.deezer.api.DeezerTrackID
import me.squeezymo.migrator.deezer.api.domain.data.DeezerAlbum
import me.squeezymo.migrator.deezer.api.domain.data.DeezerArtist
import me.squeezymo.migrator.deezer.api.domain.data.DeezerPlaylist
import me.squeezymo.migrator.deezer.api.domain.data.DeezerTrack
import me.squeezymo.oauth.api.exception.ApiException
import me.squeezymo.oauth.api.exception.NoAccessTokenException

interface IDeezerRepository {

    @Throws(NoAccessTokenException::class, ApiException::class)
    suspend fun requestArtists(): List<DeezerArtist>

    @Throws(NoAccessTokenException::class, ApiException::class)
    suspend fun requestAlbums(): List<DeezerAlbum>

    @Throws(NoAccessTokenException::class, ApiException::class)
    suspend fun requestTracks(
        excludeExternalIds: Boolean
    ): List<EntityWithExternalIDs<DeezerTrack>>

    @Throws(NoAccessTokenException::class, ApiException::class)
    suspend fun saveTracks(
        ids: Set<DeezerTrackID>
    ): Flow<Map<DeezerTrackID, StatelessResult>>

    @Throws(NoAccessTokenException::class, ApiException::class)
    suspend fun saveTracksToPlaylist(
        playlistId: DeezerPlaylistID,
        ids: Set<DeezerTrackID>
    ): Flow<Map<DeezerTrackID, StatelessResult>>

    @Throws(NoAccessTokenException::class, ApiException::class)
    suspend fun createPlaylist(
        playlistTitle: PlaylistTitle
    ): DeezerPlaylistID

    @Throws(NoAccessTokenException::class, ApiException::class)
    suspend fun requestPlaylists(): List<DeezerPlaylist>

    @Throws(NoAccessTokenException::class, ApiException::class)
    suspend fun findTrack(
        track: String,
        artist: String? = null,
        album: String? = null,
        externalIds: Map<StreamingServiceID, ID> = emptyMap()
    ): EntityWithExternalIDs<DeezerTrack>?

    suspend fun addExternalTrackId(
        trackId: DeezerTrackID,
        externalIds: Map<StreamingServiceID, ID>
    )

    suspend fun extractTrackIds(
        track: DeezerTrack,
        excludeExternalTrackIds: Boolean = false
    ): Map<StreamingServiceID, ID>

}
