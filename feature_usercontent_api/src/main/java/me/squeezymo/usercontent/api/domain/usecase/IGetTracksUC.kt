package me.squeezymo.usercontent.api.domain.usecase

import me.squeezymo.core.domain.data.BaseTrack
import me.squeezymo.core.domain.data.EntityWithExternalIDs
import me.squeezymo.core.domain.data.StreamingServiceID

interface IGetTracksUC {

    suspend fun getTracks(
        serviceId: StreamingServiceID,
        excludeExternalIds: Boolean = false
    ): List<EntityWithExternalIDs<BaseTrack>>

}
