package me.squeezymo.migrator.spotify.impl.network.dto

import me.squeezymo.migrator.spotify.api.*

internal data class SpotifyUserProfileDTO(
    val id: SpotifyUserID
)

internal data class SpotifyPlaylistDTO(
    val id: SpotifyPlaylistID,
    val name: String,
    val description: String?,
    val images: List<ImageDTO>,
    val collaborative: Boolean,
    val public: String,
    val href: String,
    val tracks: TracksDTO
)

internal data class SpotifyTrackItemDTO(
    val track: SpotifyTrackDTO
)

internal data class SpotifyTrackDTO(
    val id: SpotifyTrackID,
    val name: String,
    val album: SpotifyAlbumDTO?,
    val artists: List<SpotifyArtistDTO>,
    val durationMs: Long?,
    val previewUrl: String?
)

internal data class SpotifyAlbumDTO(
    val id: SpotifyAlbumID,
    val name: String,
    val images: List<ImageDTO>
)

internal data class SpotifyArtistDTO(
    val id: SpotifyArtistID,
    val name: String
)

internal data class ImageDTO(
    val height: Int,
    val width: Int,
    val url: String
)

internal data class TracksDTO(
    val href: String,
    val total: Int
)
