package me.squeezymo.migrator.deezer.api.domain.data

import me.squeezymo.migrator.deezer.api.DeezerAlbumID

data class DeezerAlbum(
    val id: DeezerAlbumID,
    val title: String,
    val artist: DeezerArtist,
    val imageUrl: String?
)
