package me.squeezymo.migrator.deezer.impl.network.dto

import com.google.gson.annotations.SerializedName
import me.squeezymo.migrator.deezer.api.DeezerAlbumID
import me.squeezymo.migrator.deezer.api.DeezerArtistID
import me.squeezymo.migrator.deezer.api.DeezerPlaylistID
import me.squeezymo.migrator.deezer.api.DeezerTrackID
import me.squeezymo.migrator.deezer.impl.network.query.DeezerPaginableData

data class DeezerAlbumDTO(
    val id: DeezerAlbumID,
    val title: String,
    val artist: DeezerArtistDTO,
    val cover: String?,
    val coverSmall: String?,
    val coverMedium: String?,
    val coverBig: String?
)

data class DeezerArtistDTO(
    val id: DeezerArtistID,
    val name: String,
    val picture: String?,
    val pictureSmall: String?,
    val pictureMedium: String?,
    val pictureBig: String?
)

data class DeezerTrackDTO(
    val id: DeezerTrackID,
    val title: String,
    @SerializedName("duration") val durationSeconds: Int?,
    val album: DeezerAlbumDTO?,
    val artist: DeezerArtistDTO?,
    val preview: String?
)

data class DeezerPlaylistShortDTO(
    val id: DeezerPlaylistID,
    val title: String,
    val pictureSmall: String?,
    val pictureMedium: String?,
    val pictureBig: String?
)

class DeezerPlaylistFullDTO(
    val id: DeezerPlaylistID,
    val title: String,
    val tracks: DeezerPaginableData<DeezerTrackDTO>,
    val pictureSmall: String?,
    val pictureMedium: String?,
    val pictureBig: String?
)
