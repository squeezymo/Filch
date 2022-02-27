package me.squeezymo.migrator.youtube.impl.domain

import me.squeezymo.core.domain.data.EntityWithExternalIDs
import me.squeezymo.migrator.youtube.api.domain.IYouTubeRepository
import me.squeezymo.migrator.youtube.api.domain.data.YoutubePlaylist
import me.squeezymo.migrator.youtube.api.domain.data.YoutubeTrack
import me.squeezymo.migrator.youtube.impl.network.YouTubeDataSource
import me.squeezymo.migrator.youtube.impl.network.YouTubeDataSourceFactory
import me.squeezymo.oauth.api.IOAuth2Broker
import me.squeezymo.streamingservices.api.domain.data.StreamingService
import javax.inject.Inject

internal class YouTubeRepository @Inject constructor(
    dataSourceFactory: YouTubeDataSourceFactory,
    oAuth2Broker: IOAuth2Broker
) : IYouTubeRepository {

    private val dataSource: YouTubeDataSource by lazy {
        dataSourceFactory.create(
            requireNotNull(oAuth2Broker.getOAuth2ServiceById(StreamingService.YOUTUBE.id))
        )
    }

    override suspend fun requestTracks(
        excludeExternalIds: Boolean
    ): List<EntityWithExternalIDs<YoutubeTrack>> {
        TODO("Not yet implemented")
    }

    override suspend fun requestPlaylists(): List<YoutubePlaylist> {
        TODO("Not yet implemented")

        dataSource.getPlaylists().body()!!.items.forEach {
            dataSource.getPlaylistItems(it.id)
        }
    }

}
