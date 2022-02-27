package me.squeezymo.usercontent.impl.ui.viewobject

import me.squeezymo.core.domain.data.ID
import me.squeezymo.core.ui.recyclerview.IListItem
import me.squeezymo.streamingservices.api.domain.data.StreamingService

internal sealed class UserContentItemUi : IListItem {

    data class Playlists(
        val items: List<PlaylistUi>
    ) : UserContentItemUi() {

        override fun isItemTheSameAs(another: IListItem): Boolean =
            another is Playlists

        override fun isContentTheSameAs(another: IListItem): Boolean =
            this == another

    }

    object TracksLoading : UserContentItemUi() {

        override fun isItemTheSameAs(another: IListItem): Boolean =
            another is TracksLoading

        override fun isContentTheSameAs(another: IListItem): Boolean =
            true

    }

    data class Track(
        val id: ID,
        val title: String,
        val artist: String?,
        val audioPreviewUrl: String?,
        val duration: String?,
        val thumbnail: TrackThumbnailUi,
        val isInSelectionMode: Boolean,
        val migrationStatus: MigrationStatusUi,
        val dstService: StreamingService
    ) : UserContentItemUi() {

        override fun isItemTheSameAs(another: IListItem): Boolean =
            another is Track && id == another.id

        override fun isContentTheSameAs(another: IListItem): Boolean =
            this == another

    }

}
