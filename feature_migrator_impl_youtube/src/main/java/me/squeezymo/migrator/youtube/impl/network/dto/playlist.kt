package me.squeezymo.migrator.youtube.impl.network.dto

import me.squeezymo.migrator.youtube.api.YouTubePlaylistID

internal class PlaylistDTO(
    val id: YouTubePlaylistID,
    val snippet: SnippetDTO
)

internal class SnippetDTO(
    val title: String,
    val description: String,
    val thumbnails: Map<String, ThumbnailDTO>
)

internal class ThumbnailDTO(
    val url: String,
    val width: Int,
    val height: Int
)

internal class PlaylistItemDTO(

)