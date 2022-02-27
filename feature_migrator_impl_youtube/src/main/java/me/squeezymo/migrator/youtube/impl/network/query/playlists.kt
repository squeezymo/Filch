package me.squeezymo.migrator.youtube.impl.network.query

import me.squeezymo.migrator.youtube.impl.network.dto.PageInfoDTO
import me.squeezymo.migrator.youtube.impl.network.dto.PlaylistDTO
import me.squeezymo.migrator.youtube.impl.network.dto.PlaylistItemDTO

internal class PlaylistsResponse(
    val kind: String,
    val pageInfo: PageInfoDTO,
    val items: List<PlaylistDTO>
)

internal class PlaylistItemsResponse(
    val kind: String,
    val pageInfo: PageInfoDTO,
    val items: List<PlaylistItemDTO>
)
