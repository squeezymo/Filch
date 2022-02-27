package me.squeezymo.migrator.spotify.api.domain.data

import me.squeezymo.migrator.spotify.api.SpotifyArtistID

data class SpotifyArtist(
    val id: SpotifyArtistID,
    val name: String
)
