package me.squeezymo.migrator.spotify.api.domain.data

import me.squeezymo.migrator.spotify.api.SpotifyTrackID

data class SpotifyTrack(
    val id: SpotifyTrackID,
    val name: String,
    val album: SpotifyAlbum?,
    val artists: List<SpotifyArtist>,
    val durationMillis: Long?,
    val previewUrl: String?
)