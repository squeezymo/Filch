package me.squeezymo.migrator.spotify.api.domain

import kotlinx.coroutines.flow.Flow
import me.squeezymo.core.domain.data.*
import me.squeezymo.migrator.spotify.api.SpotifyPlaylistID
import me.squeezymo.migrator.spotify.api.SpotifyTrackID
import me.squeezymo.migrator.spotify.api.domain.data.SpotifyPlaylist
import me.squeezymo.migrator.spotify.api.domain.data.SpotifyTrack
import me.squeezymo.oauth.api.exception.ApiException
import me.squeezymo.oauth.api.exception.NoAccessTokenException

interface ISpotifyRepository {

   @Throws(NoAccessTokenException::class, ApiException::class)
   suspend fun requestTracks(
      excludeExternalIds: Boolean
   ): List<EntityWithExternalIDs<SpotifyTrack>>

   @Throws(NoAccessTokenException::class, ApiException::class)
   suspend fun saveTracks(
      ids: Set<SpotifyTrackID>
   ): Flow<Map<SpotifyTrackID, StatelessResult>>

   @Throws(NoAccessTokenException::class, ApiException::class)
   suspend fun saveTracksToPlaylist(
      playlistId: SpotifyPlaylistID,
      ids: Set<SpotifyTrackID>
   ): Flow<Map<SpotifyTrackID, StatelessResult>>

   @Throws(NoAccessTokenException::class, ApiException::class)
   suspend fun createPlaylist(
      playlistTitle: PlaylistTitle
   ): SpotifyPlaylistID

   @Throws(NoAccessTokenException::class, ApiException::class)
   suspend fun requestPlaylists(): List<SpotifyPlaylist>

   @Throws(NoAccessTokenException::class, ApiException::class)
   suspend fun findTrack(
      track: String,
      artist: String?,
      album: String? = null,
      year: Int? = null,
      externalIds: Map<StreamingServiceID, ID> = emptyMap()
   ): EntityWithExternalIDs<SpotifyTrack>?

   suspend fun addExternalTrackId(
      trackId: SpotifyTrackID,
      externalIds: Map<StreamingServiceID, ID>
   )

   suspend fun extractTrackIds(
      track: SpotifyTrack,
      excludeExternalTrackIds: Boolean = false
   ): Map<StreamingServiceID, ID>

}
