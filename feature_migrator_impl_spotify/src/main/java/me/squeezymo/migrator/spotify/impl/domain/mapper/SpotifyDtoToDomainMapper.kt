package me.squeezymo.migrator.spotify.impl.domain.mapper

import me.squeezymo.migrator.spotify.api.domain.data.SpotifyAlbum
import me.squeezymo.migrator.spotify.api.domain.data.SpotifyArtist
import me.squeezymo.migrator.spotify.api.domain.data.SpotifyPlaylist
import me.squeezymo.migrator.spotify.api.domain.data.SpotifyTrack
import me.squeezymo.migrator.spotify.impl.network.dto.*
import javax.inject.Inject

internal interface ISpotifyDtoToDomainMapper {

    fun mapPlaylist(
        playlist: SpotifyPlaylistDTO,
        trackItems: List<SpotifyTrackItemDTO>
    ): SpotifyPlaylist

    fun mapTrackItem(
        trackItem: SpotifyTrackItemDTO
    ): SpotifyTrack

    fun mapTrack(
        track: SpotifyTrackDTO
    ): SpotifyTrack

    fun mapAlbum(
        album: SpotifyAlbumDTO
    ): SpotifyAlbum

    fun mapArtist(
        album: SpotifyArtistDTO
    ): SpotifyArtist

}

internal class SpotifyDtoToDomainMapper @Inject constructor(): ISpotifyDtoToDomainMapper {

    override fun mapPlaylist(
        playlist: SpotifyPlaylistDTO,
        trackItems: List<SpotifyTrackItemDTO>
    ): SpotifyPlaylist {
        return SpotifyPlaylist(
            id = playlist.id,
            name = playlist.name,
            description = playlist.description,
            imageUrl = playlist.images.firstOrNull()?.url,
            tracks = trackItems.map(::mapTrackItem)
        )
    }

    override fun mapTrackItem(
        trackItem: SpotifyTrackItemDTO
    ): SpotifyTrack {
        return mapTrack(trackItem.track)
    }

    override fun mapTrack(
        track: SpotifyTrackDTO
    ): SpotifyTrack {
        return SpotifyTrack(
            id = track.id,
            name = track.name,
            album = track.album?.let(::mapAlbum),
            artists = track.artists.map(::mapArtist),
            durationMillis = track.durationMs,
            previewUrl = track.previewUrl
        )
    }

    override fun mapAlbum(
        album: SpotifyAlbumDTO
    ): SpotifyAlbum {
        return SpotifyAlbum(
            id = album.id,
            name = album.name,
            imageUrl = album.images.lastOrNull()?.url
        )
    }

    override fun mapArtist(
        artist: SpotifyArtistDTO
    ): SpotifyArtist {
        return SpotifyArtist(
            id = artist.id,
            name = artist.name
        )
    }

}
