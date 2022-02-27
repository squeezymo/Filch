package me.squeezymo.usercontent.api.domain.usecase

import me.squeezymo.core.domain.data.ID
import me.squeezymo.core.domain.data.StreamingServiceID

interface IAddTrackExternalIdUC {

    suspend fun addTrackExternalId(
        serviceId: StreamingServiceID,
        trackId: ID,
        externalIds: Map<StreamingServiceID, ID>
    )

}
