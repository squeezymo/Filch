package me.squeezymo.migrator.deezer.impl.domain.mapper

import me.squeezymo.migrator.deezer.api.domain.data.DeezerAlbum
import me.squeezymo.migrator.deezer.api.domain.data.DeezerArtist
import me.squeezymo.migrator.deezer.api.domain.data.DeezerPlaylist
import me.squeezymo.migrator.deezer.api.domain.data.DeezerTrack
import me.squeezymo.migrator.deezer.impl.network.dto.DeezerAlbumDTO
import me.squeezymo.migrator.deezer.impl.network.dto.DeezerArtistDTO
import me.squeezymo.migrator.deezer.impl.network.dto.DeezerPlaylistFullDTO
import me.squeezymo.migrator.deezer.impl.network.dto.DeezerTrackDTO
import javax.inject.Inject

internal interface IDeezerDtoToDomainMapper {

    fun mapPlaylist(
        playlist: DeezerPlaylistFullDTO,
        tracks: List<DeezerTrackDTO>
    ): DeezerPlaylist

    fun mapTrack(
        track: DeezerTrackDTO
    ): DeezerTrack

    fun mapAlbum(
        album: DeezerAlbumDTO,
        artist: DeezerArtistDTO? = null
    ): DeezerAlbum

    fun mapArtist(
        artist: DeezerArtistDTO
    ): DeezerArtist

}

internal class DeezerDtoToDomainMapper @Inject constructor(): IDeezerDtoToDomainMapper {

    override fun mapPlaylist(
        playlist: DeezerPlaylistFullDTO,
        tracks: List<DeezerTrackDTO>
    ): DeezerPlaylist {
        return DeezerPlaylist(
            id = playlist.id,
            title = playlist.title,
            tracks = tracks.map(::mapTrack),
            pictureSmall = playlist.pictureSmall,
            pictureMedium = playlist.pictureMedium,
            pictureBig = playlist.pictureBig
        )
    }

    override fun mapTrack(track: DeezerTrackDTO): DeezerTrack {
        return DeezerTrack(
            id = track.id,
            title = track.title,
            album = track.album?.let { mapAlbum(it, track.artist) },
            artist = track.artist?.let(::mapArtist),
            durationSeconds = track.durationSeconds,
            previewUrl = track.preview
        )
    }

    override fun mapAlbum(album: DeezerAlbumDTO, artist: DeezerArtistDTO?): DeezerAlbum {
        return DeezerAlbum(
            id = album.id,
            title = album.title,
            artist = artist?.let(::mapArtist) ?: album.artist.let(::mapArtist),
            imageUrl = album.coverSmall ?: album.coverMedium ?: album.cover
        )
    }

    override fun mapArtist(artist: DeezerArtistDTO): DeezerArtist {
        return DeezerArtist(
            id = artist.id,
            name = artist.name,
            imageUrl = artist.pictureSmall ?: artist.picture
        )
    }

}
