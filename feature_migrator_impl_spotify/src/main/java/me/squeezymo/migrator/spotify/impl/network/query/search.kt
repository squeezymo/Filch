package me.squeezymo.migrator.spotify.impl.network.query

import me.squeezymo.migrator.spotify.impl.network.contract.SupportsPaging
import me.squeezymo.migrator.spotify.impl.network.dto.SpotifyTrackDTO

internal data class SpotifySearchResponse(
    val tracks: Tracks?
) {

    data class Tracks(
        val items: List<SpotifyTrackDTO>,
        val limit: Int,
        val offset: Int,
        val previous: String?,
        val next: String?,
        val total: Int
    ) : SupportsPaging {

        override val nextBatchUrl: String?
            get() = next

    }

}
