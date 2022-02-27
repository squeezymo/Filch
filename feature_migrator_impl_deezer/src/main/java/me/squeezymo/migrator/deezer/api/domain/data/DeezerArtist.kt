package me.squeezymo.migrator.deezer.api.domain.data

import me.squeezymo.migrator.deezer.api.DeezerArtistID

data class DeezerArtist(
    val id: DeezerArtistID,
    val name: String,
    val imageUrl: String?
)
