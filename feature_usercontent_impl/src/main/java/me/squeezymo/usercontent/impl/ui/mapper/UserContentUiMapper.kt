package me.squeezymo.usercontent.impl.ui.mapper

import me.squeezymo.core.domain.data.ID
import me.squeezymo.core.domain.data.PlaylistTitle
import me.squeezymo.streamingservices.api.domain.data.StreamingService
import me.squeezymo.usercontent.impl.ui.data.*
import me.squeezymo.usercontent.impl.ui.uistate.SelectionModeUiState
import me.squeezymo.usercontent.impl.ui.uistate.ShowMigratedTracksUiState
import me.squeezymo.usercontent.impl.ui.uistate.UserContentUiState
import me.squeezymo.usercontent.impl.ui.viewobject.MigrationStatusUi
import me.squeezymo.usercontent.impl.ui.viewobject.PlaylistUi
import me.squeezymo.usercontent.impl.ui.viewobject.UserContentItemUi

internal class UserContentUiMapper(
    private val trackUiMapper: TrackUiMapper
) {

    fun createUiState(
        tracksUiData: TracksUiData,
        playlistsUiData: PlaylistsUiData,
        isInSelectionMode: Boolean,
        showMigratedTracks: Boolean,
        dstService: StreamingService
    ): UserContentUiState {
        val uiContent = ArrayList<UserContentItemUi>()
        var migratedTracksCnt = 0
        var nonMigratedTracksCnt = 0
        var migratedPlaylistsCnt = 0
        var nonMigratedPlaylistsCnt = 0

        if (playlistsUiData is PlaylistsUiData.Show) {
            val playlistsSearchState = playlistsUiData.playlistsSearchState

            if (playlistsSearchState?.playlistProgresses == null ||
                playlistsSearchState.playlistProgresses.isEmpty()
            ) {
                uiContent.add(
                    UserContentItemUi.Playlists(
                        listOf(
                            PlaylistUi.Loading(null)
                        )
                    )
                )
            } else {
                uiContent.add(
                    UserContentItemUi.Playlists(
                        playlistsSearchState
                            .playlistProgresses
                            .map { playlistProgress ->
                                if (playlistProgress.inProgress) {
                                    PlaylistUi.Loading(playlistProgress.playlist.title)
                                } else {
                                    PlaylistUi.Loaded(
                                        playlistProgress.playlist.title,
                                        playlistProgress.playlist.thumbnailUrl,
                                        isInSelectionMode,
                                        createMigrationStatusUi(
                                            playlistProgress.playlist.id,
                                            playlistsSearchState,
                                            playlistsUiData.playlistsMigrationState,
                                            playlistsUiData.playlistsSelectedForMigration
                                        ),
                                        dstService
                                    )
                                }
                            }
                    )
                )
            }
        }

        if (tracksUiData is TracksUiData.Show) {
            val tracksSearchState = tracksUiData.tracksSearchState

            if (tracksSearchState != null) {
                if (tracksSearchState.inProgress) {
                    uiContent.add(UserContentItemUi.TracksLoading)
                }

                for ((srcId, _) in tracksSearchState.srcIdToDstId) {
                    val migrationStatusUi = createMigrationStatusUi(
                        srcId,
                        tracksSearchState,
                        tracksUiData.tracksMigrationState,
                        tracksUiData.tracksSelectedForMigration
                    )

                    if (migrationStatusUi is MigrationStatusUi.Migrated) {
                        migratedTracksCnt++
                    } else if (migrationStatusUi is MigrationStatusUi.NotMigrated) {
                        nonMigratedTracksCnt++
                    }

                    if (migrationStatusUi !is MigrationStatusUi.Migrated || showMigratedTracks) {
                        uiContent.add(
                            trackUiMapper.mapTrack(
                                srcId,
                                requireNotNull(tracksSearchState.srcIdToTrack[srcId]).entity,
                                isInSelectionMode,
                                migrationStatusUi,
                                dstService
                            )
                        )
                    }
                }
            }
        }

        return UserContentUiState(
            userContent = uiContent,
            tracksInSourceService = (tracksUiData as? TracksUiData.Show)
                ?.tracksSearchState
                ?.srcIdToTrack
                ?.size?.takeUnless { it == 0 },
            tracksInDestinationService = (tracksUiData as? TracksUiData.Show)
                ?.tracksSearchState
                ?.srcIdToDstId
                ?.size,
            tracksToMigrate = if (isInSelectionMode) {
                (tracksUiData as? TracksUiData.Show)?.tracksSelectedForMigration?.size
            } else {
                nonMigratedTracksCnt
            },
            playlistsInSourceService = (playlistsUiData as? PlaylistsUiData.Show)
                ?.playlistsSearchState
                ?.srcTitleToPlaylist
                ?.size?.takeUnless { it == 0 },
            playlistsInDestinationService = (playlistsUiData as? PlaylistsUiData.Show)
                ?.playlistsSearchState
                ?.dstTitleToPlaylist
                ?.size?.takeUnless { it == 0 },
            playlistsToMigrate = if (isInSelectionMode) {
                (playlistsUiData as? PlaylistsUiData.Show)?.playlistsSelectedForMigration?.size
            } else {
                nonMigratedPlaylistsCnt
            },
            showMigratedTracksUiState = if (migratedTracksCnt > 0) {
                if (showMigratedTracks) {
                    ShowMigratedTracksUiState.Shown
                } else {
                    ShowMigratedTracksUiState.Hidden
                }
            } else {
                ShowMigratedTracksUiState.None
            },
            searchInProgress = (tracksUiData as? TracksUiData.Show)?.tracksSearchState?.inProgress ?: true,
            progress = (tracksUiData as? TracksUiData.Show)?.tracksSearchState?.progress ?: 0,
            selectionModeUiState = when {
                nonMigratedTracksCnt == 0 -> {
                    SelectionModeUiState.None
                }
                isInSelectionMode -> {
                    SelectionModeUiState.On
                }
                else -> {
                    SelectionModeUiState.Off
                }
            }
        )
    }

    fun createMigrationStatusUi(
        srcId: ID,
        tracksSearchState: TracksSearchState,
        tracksMigrationState: Map<ID, TrackMigrationState>,
        tracksSelectedForMigration: Set<ID>
    ): MigrationStatusUi {
        return when {
            tracksSearchState.inProgress -> {
                MigrationStatusUi.Unknown
            }
            tracksMigrationState[srcId] is TrackMigrationState.InProgress -> {
                MigrationStatusUi.InProgress
            }
            tracksMigrationState[srcId] is TrackMigrationState.Migrated ||
                    tracksSearchState.dstIdToTrack.containsKey(tracksSearchState.srcIdToDstId[srcId]) -> {
                MigrationStatusUi.Migrated
            }
            else -> {
                MigrationStatusUi.NotMigrated(
                    isSelected = srcId in tracksSelectedForMigration
                )
            }
        }
    }

    private fun createMigrationStatusUi(
        srcPlaylistId: ID,
        playlistsSearchState: PlaylistsSearchState,
        playlistsMigrationState: Map<PlaylistTitle, PlaylistMigrationState>,
        playlistsSelectedForMigration: Set<ID>
    ): MigrationStatusUi {
        return when {
            playlistsSearchState.inProgress -> {
                MigrationStatusUi.Unknown
            }
            playlistsMigrationState[srcPlaylistId] is PlaylistMigrationState.InProgress -> {
                MigrationStatusUi.InProgress
            }
            playlistsMigrationState[srcPlaylistId] is PlaylistMigrationState.Migrated ||
                    playlistsSearchState.dstTitleToPlaylist[srcPlaylistId] != null -> {
                MigrationStatusUi.Migrated
            }
            else -> {
                MigrationStatusUi.NotMigrated(
                    isSelected = srcPlaylistId in playlistsSelectedForMigration
                )
            }
        }
    }

}
