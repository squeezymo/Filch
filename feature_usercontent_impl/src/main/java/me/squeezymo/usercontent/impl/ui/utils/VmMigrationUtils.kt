package me.squeezymo.usercontent.impl.ui.utils

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import me.squeezymo.core.domain.data.ID
import me.squeezymo.core.domain.data.PlaylistTitle
import me.squeezymo.core.domain.data.StatelessResult
import me.squeezymo.core.ext.filterNotNullKeys
import me.squeezymo.streamingservices.api.domain.data.StreamingService
import me.squeezymo.usercontent.api.domain.usecase.IAddTracksToLibraryUC
import me.squeezymo.usercontent.api.domain.usecase.IAddTracksToPlaylistUC
import me.squeezymo.usercontent.api.domain.usecase.ICreatePlaylistUC
import me.squeezymo.usercontent.impl.domain.data.CompositeTrackId
import me.squeezymo.usercontent.impl.ui.data.PlaylistMigrationState
import me.squeezymo.usercontent.impl.ui.data.PlaylistTracksMigrationState
import me.squeezymo.usercontent.impl.ui.data.TrackMigrationState

internal class VmMigrationUtils(
    private val toService: StreamingService
) {

    fun migrateTracksToLibrary(
        addTracksToLibraryUC: IAddTracksToLibraryUC,
        srcTrackIds: Set<ID>,
        srcToDstTrackId: Map<ID, ID>
    ): Flow<Map<CompositeTrackId, TrackMigrationState>> {
        return migrateTracks(
            srcTrackIds,
            srcToDstTrackId
        ) { ids ->
            addTracksToLibraryUC.addTracksToLibrary(toService.id, ids)
        }.map {
            it.trackIdToMigrationState
        }
    }

    fun migrateTracksToPlaylist(
        createPlaylistUC: ICreatePlaylistUC,
        addTracksToPlaylistUC: IAddTracksToPlaylistUC,
        playlistTitle: PlaylistTitle,
        dstPlaylistId: ID?,
        srcTrackIds: Set<ID>,
        srcToDstTrackId: Map<ID, ID>
    ): Flow<PlaylistTracksMigrationState> {
        return flow {
            val adjustedDstPlaylistId: ID = if (dstPlaylistId == null) {
                val dstPlaylist =
                    createPlaylistUC.createPlaylist(toService.id, playlistTitle)
                dstPlaylist.id
            } else {
                dstPlaylistId
            }

            migrateTracks(
                srcTrackIds,
                srcToDstTrackId
            ) { ids ->
                addTracksToPlaylistUC.addTracksToPlaylist(
                    toService.id,
                    adjustedDstPlaylistId,
                    ids
                )
            }.collect { tracksMigrationState ->
                emit(
                    PlaylistTracksMigrationState(
                        playlistId = adjustedDstPlaylistId,
                        tracksMigrationState = tracksMigrationState.trackIdToMigrationState,
                        playlistMigrationState = when {
                            tracksMigrationState.isFinal -> PlaylistMigrationState.Migrated
                            else -> PlaylistMigrationState.InProgress
                        }
                    )
                )
            }
        }
    }

    private fun migrateTracks(
        srcTrackIds: Set<ID>,
        srcToDstTrackId: Map<ID, ID>,
        migrate: suspend (
            ids: Set<ID>
        ) -> Flow<Map<ID, StatelessResult>>
    ): Flow<TotalMigrationState> = flow {
        val filteredSrcTrackIds = srcTrackIds.filter(srcToDstTrackId::containsKey)
        var trackIdToMigrationState: Map<CompositeTrackId, TrackMigrationState> =
            filteredSrcTrackIds
                .map { srcId ->
                    CompositeTrackId(srcId, srcToDstTrackId[srcId])
                }
                .associateWith { TrackMigrationState.InProgress }

        emit(
            TotalMigrationState(
                trackIdToMigrationState = trackIdToMigrationState,
                isFinal = false
            )
        )

        val dstTrackIds = srcTrackIds.mapNotNullTo(HashSet(), srcToDstTrackId::get)
        val dstToSrcTrackId: MutableMap<ID, ID> = HashMap()
        for ((srcId, dstId) in srcToDstTrackId) {
            dstToSrcTrackId[dstId] = srcId
        }

        migrate(dstTrackIds)
            .collect { results: Map<ID, StatelessResult> ->
                val migrationInProgressTracks = dstTrackIds
                    .filter { it !in results }
                    .associateWith { TrackMigrationState.InProgress }
                    .mapKeys { (dstId, _) ->
                        dstToSrcTrackId[dstId]?.let { CompositeTrackId(it, dstId) }
                    }
                    .filterNotNullKeys()

                val migrationCompleteTracks = results
                    .mapValues { (_, result) ->
                        when (result) {
                            StatelessResult.Success -> TrackMigrationState.Migrated
                            StatelessResult.Error -> TrackMigrationState.Error
                        }
                    }
                    .mapKeys { (dstId, _) ->
                        dstToSrcTrackId[dstId]?.let { CompositeTrackId(it, dstId) }
                    }
                    .filterNotNullKeys()

                trackIdToMigrationState = migrationInProgressTracks + migrationCompleteTracks

                emit(
                    TotalMigrationState(
                        trackIdToMigrationState = trackIdToMigrationState,
                        isFinal = false
                    )
                )
            }

        emit(
            TotalMigrationState(
                trackIdToMigrationState = trackIdToMigrationState,
                isFinal = true
            )
        )
    }

    private data class TotalMigrationState(
        val trackIdToMigrationState: Map<CompositeTrackId, TrackMigrationState>,
        val isFinal: Boolean
    )

}
