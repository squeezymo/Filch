package me.squeezymo.migrator.deezer.api.domain.data

import me.squeezymo.migrator.deezer.api.DeezerPlaylistID

data class DeezerPlaylist(
    val id: DeezerPlaylistID,
    val title: String,
    val tracks: List<DeezerTrack>,
    val pictureSmall: String?,
    val pictureMedium: String?,
    val pictureBig: String?
)
