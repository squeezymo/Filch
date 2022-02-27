package me.squeezymo.migrator.spotify.impl.network.query

import me.squeezymo.core.domain.data.PlaylistTitle
import me.squeezymo.migrator.spotify.impl.network.contract.SupportsPaging
import me.squeezymo.migrator.spotify.impl.network.dto.SpotifyPlaylistDTO

internal data class SpotifyPlaylistsResponse(
    val items: List<SpotifyPlaylistDTO>,
    val limit: Int,
    val offset: Int,
    val previous: String?,
    val next: String?,
    val total: Int
) : SupportsPaging {

    override val nextBatchUrl: String?
        get() = next

}

internal data class SpotifyCreatePlaylistBody(
    val name: PlaylistTitle,
    val public: Boolean,
    val collaborative: Boolean,
    val description: String
)

internal data class SpotifyAddToPlaylistBody(
    val uris: List<String>
)

internal data class SpotifyAddToPlaylistResponse(
    val snapshotId: String
)