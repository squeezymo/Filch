package me.squeezymo.usercontent.api.domain.usecase

import me.squeezymo.core.domain.data.BaseTrack
import me.squeezymo.core.domain.data.EntityWithExternalIDs
import me.squeezymo.core.domain.data.ID
import me.squeezymo.core.domain.data.StreamingServiceID

interface IFindTrackIdUC {

   suspend fun findTrackId(
       fromServiceId: StreamingServiceID,
       toServiceId: StreamingServiceID,
       srcTrack: EntityWithExternalIDs<BaseTrack>
   ): ID?

}
