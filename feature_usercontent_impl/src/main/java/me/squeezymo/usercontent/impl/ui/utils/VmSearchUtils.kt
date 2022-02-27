package me.squeezymo.usercontent.impl.ui.utils

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import me.squeezymo.core.domain.data.*
import me.squeezymo.streamingservices.api.domain.data.StreamingService
import me.squeezymo.usercontent.api.domain.usecase.IFindTrackIdUC
import me.squeezymo.usercontent.impl.ui.data.PlaylistProgress
import me.squeezymo.usercontent.impl.ui.data.PlaylistsSearchState
import me.squeezymo.usercontent.impl.ui.data.TracksSearchState

internal class VmSearchUtils(
    private val fromService: StreamingService,
    private val toService: StreamingService
) {

    fun searchForTracks(
        findTrackIdUC: IFindTrackIdUC,
        srcIdToTrack: Map<ID, EntityWithExternalIDs<BaseTrack>>,
        dstIdToTrack: Map<ID, EntityWithExternalIDs<BaseTrack>>
    ): Flow<TracksSearchState> = flow {
        val srcIdToDstId = LinkedHashMap<ID, ID>()
        var iterationCounter = 0

        emit(
            TracksSearchState(
                0,
                true,
                srcIdToTrack,
                dstIdToTrack,
                LinkedHashMap(srcIdToDstId)
            )
        )

        for ((srcId, srcTrack) in srcIdToTrack) {
            val dstId = findTrackIdUC.findTrackId(
                fromService.id,
                toService.id,
                srcTrack
            )

            if (dstId != null) {
                srcIdToDstId[srcId] = dstId

                emit(
                    TracksSearchState(
                        (100F * (iterationCounter + 1) / srcIdToTrack.size).toInt(),
                        true,
                        srcIdToTrack,
                        dstIdToTrack,
                        LinkedHashMap(srcIdToDstId)
                    )
                )
            }

            iterationCounter++
        }

        emit(
            TracksSearchState(
                100,
                false,
                srcIdToTrack,
                dstIdToTrack,
                LinkedHashMap(srcIdToDstId)
            )
        )
    }

    fun searchForPlaylists(
        findTrackIdUC: IFindTrackIdUC,
        srcTitleToPlaylist: Map<PlaylistTitle, BasePlaylist>,
        dstTitleToPlaylist: Map<PlaylistTitle, BasePlaylist>
    ): Flow<PlaylistsSearchState> = flow {
        emit(
            PlaylistsSearchState(
                true,
                srcTitleToPlaylist.values.map { playlist ->
                    PlaylistProgress(playlist, true)
                },
                srcTitleToPlaylist,
                dstTitleToPlaylist,
                emptyMap()
            )
        )

        val srcIdToDstIdWithinPlaylist = LinkedHashMap<ID, ID>()

        for ((_, srcPlaylist) in srcTitleToPlaylist) {
            srcPlaylist.tracks.forEach { srcTrack ->
                val srcId = requireNotNull(srcTrack.ids[fromService.id])
                val externalIds = HashMap(srcTrack.ids)
                externalIds[fromService.id] = srcId

                val dstId = findTrackIdUC.findTrackId(
                    fromService.id,
                    toService.id,
                    srcTrack
                )

                if (dstId != null) {
                    srcIdToDstIdWithinPlaylist[srcId] = dstId

                    emit(
                        PlaylistsSearchState(
                            true,
                            srcTitleToPlaylist.values.map { playlist ->
                                PlaylistProgress(playlist, true)
                            },
                            srcTitleToPlaylist,
                            dstTitleToPlaylist,
                            LinkedHashMap(srcIdToDstIdWithinPlaylist)
                        )
                    )
                }
            }
        }

        emit(
            PlaylistsSearchState(
                false,
                srcTitleToPlaylist.values.map { playlist ->
                    PlaylistProgress(playlist, false)
                },
                srcTitleToPlaylist,
                dstTitleToPlaylist,
                LinkedHashMap(srcIdToDstIdWithinPlaylist)
            )
        )
    }

}
