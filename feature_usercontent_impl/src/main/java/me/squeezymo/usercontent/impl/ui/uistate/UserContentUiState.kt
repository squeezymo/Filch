package me.squeezymo.usercontent.impl.ui.uistate

import androidx.annotation.IntRange
import me.squeezymo.usercontent.impl.ui.viewobject.UserContentItemUi

internal data class UserContentUiState(
    val userContent: List<UserContentItemUi>,
    val tracksInSourceService: Int?,
    val tracksInDestinationService: Int?,
    val tracksToMigrate: Int?,
    val playlistsInSourceService: Int?,
    val playlistsInDestinationService: Int?,
    val playlistsToMigrate: Int?,
    val showMigratedTracksUiState: ShowMigratedTracksUiState,
    val searchInProgress: Boolean,
    @IntRange(from = 0L, to = 100L) val progress: Int,
    val selectionModeUiState: SelectionModeUiState
) {

    companion object {

        fun loading(): UserContentUiState {
            return UserContentUiState(
                userContent = listOf(UserContentItemUi.TracksLoading),
                tracksInSourceService = null,
                tracksInDestinationService = null,
                tracksToMigrate = null,
                playlistsInSourceService = null,
                playlistsInDestinationService = null,
                playlistsToMigrate = null,
                showMigratedTracksUiState = ShowMigratedTracksUiState.None,
                searchInProgress = true,
                progress = 0,
                selectionModeUiState = SelectionModeUiState.None
            )
        }

    }

}
