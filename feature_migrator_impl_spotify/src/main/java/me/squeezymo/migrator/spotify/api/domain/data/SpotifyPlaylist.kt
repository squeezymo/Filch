package me.squeezymo.migrator.spotify.api.domain.data

import me.squeezymo.migrator.spotify.api.SpotifyPlaylistID

data class SpotifyPlaylist(
    val id: SpotifyPlaylistID,
    val name: String,
    val description: String?,
    val imageUrl: String?,
    val tracks: List<SpotifyTrack>
)