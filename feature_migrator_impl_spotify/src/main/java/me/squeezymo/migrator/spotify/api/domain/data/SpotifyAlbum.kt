package me.squeezymo.migrator.spotify.api.domain.data

import me.squeezymo.migrator.spotify.api.SpotifyAlbumID

data class SpotifyAlbum(
    val id: SpotifyAlbumID,
    val name: String,
    val imageUrl: String?
)
