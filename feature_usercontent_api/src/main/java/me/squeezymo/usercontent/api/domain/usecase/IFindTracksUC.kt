package me.squeezymo.usercontent.api.domain.usecase

import me.squeezymo.core.domain.data.BaseTrack
import me.squeezymo.core.domain.data.EntityWithExternalIDs
import me.squeezymo.core.domain.data.ID
import me.squeezymo.core.domain.data.StreamingServiceID

interface IFindTracksUC {

   suspend fun findTrack(
       serviceId: StreamingServiceID,
       track: String,
       artist: String?,
       album: String? = null,
       externalIds: Map<StreamingServiceID, ID> = emptyMap()
   ): EntityWithExternalIDs<BaseTrack>?

}
