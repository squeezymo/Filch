package me.squeezymo.migrator.youtube.api.domain

import me.squeezymo.core.domain.data.EntityWithExternalIDs
import me.squeezymo.migrator.youtube.api.domain.data.YoutubePlaylist
import me.squeezymo.migrator.youtube.api.domain.data.YoutubeTrack

interface IYouTubeRepository {

   suspend fun requestTracks(
      excludeExternalIds: Boolean
   ): List<EntityWithExternalIDs<YoutubeTrack>>

   suspend fun requestPlaylists(): List<YoutubePlaylist>

}
