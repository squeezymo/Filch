package me.squeezymo.usercontent.impl.ui.viewobject

import me.squeezymo.core.domain.data.PlaylistTitle
import me.squeezymo.core.ui.recyclerview.IListItem
import me.squeezymo.streamingservices.api.domain.data.StreamingService

sealed class PlaylistUi : IListItem {

    data class Loading(
        val title: PlaylistTitle?
    ) : PlaylistUi() {

        override fun isItemTheSameAs(another: IListItem): Boolean =
            another is Loading

        override fun isContentTheSameAs(another: IListItem): Boolean =
            true

    }

    data class Loaded(
        val title: PlaylistTitle,
        val thumbnailUrl: String?,
        val isInSelectionMode: Boolean,
        val migrationStatus: MigrationStatusUi,
        val dstService: StreamingService
    ) : PlaylistUi() {

        override fun isItemTheSameAs(another: IListItem): Boolean =
            another is Loaded && title == another.title

        override fun isContentTheSameAs(another: IListItem): Boolean =
            this == another

    }

}
