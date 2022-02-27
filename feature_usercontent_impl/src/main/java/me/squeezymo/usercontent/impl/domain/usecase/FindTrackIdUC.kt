package me.squeezymo.usercontent.impl.domain.usecase

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import me.squeezymo.core.domain.data.BaseTrack
import me.squeezymo.core.domain.data.EntityWithExternalIDs
import me.squeezymo.core.domain.data.ID
import me.squeezymo.core.domain.data.StreamingServiceID
import me.squeezymo.usercontent.api.domain.usecase.IAddTrackExternalIdUC
import me.squeezymo.usercontent.api.domain.usecase.IFindTrackIdUC
import me.squeezymo.usercontent.api.domain.usecase.IFindTracksUC
import javax.inject.Inject

internal class FindTrackIdUC @Inject constructor(
    private val findTracksUC: IFindTracksUC,
    private val addTrackExternalIdUC: IAddTrackExternalIdUC,
) : IFindTrackIdUC {
    
    override suspend fun findTrackId(
        fromServiceId: StreamingServiceID,
        toServiceId: StreamingServiceID,
        srcTrack: EntityWithExternalIDs<BaseTrack>
    ): ID? {
        val srcId = requireNotNull(srcTrack.ids[fromServiceId])
        val externalIds = HashMap(srcTrack.ids)
        externalIds[fromServiceId] = srcId

        return srcTrack.ids[toServiceId]
            ?: withContext(Dispatchers.IO) {
                findTracksUC.findTrack(
                    serviceId = toServiceId,
                    track = srcTrack.entity.title,
                    artist = srcTrack.entity.artist,
                    album = srcTrack.entity.album,
                    externalIds = externalIds
                )
            }?.ids?.get(toServiceId)?.also { dstId ->
                withContext(Dispatchers.IO) {
                    launch {
                        runCatching {
                            addTrackExternalIdUC.addTrackExternalId(
                                serviceId = fromServiceId,
                                trackId = srcId,
                                externalIds = mapOf(
                                    toServiceId to dstId
                                )
                            )
                        }
                    }
                }
            }
    }
    
}
