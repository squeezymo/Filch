package me.squeezymo.migrator.deezer.api.domain.data

import me.squeezymo.migrator.deezer.api.DeezerTrackID

data class DeezerTrack(
    val id: DeezerTrackID,
    val title: String,
    val album: DeezerAlbum?,
    val artist: DeezerArtist?,
    val durationSeconds: Int?,
    val previewUrl: String?
)
