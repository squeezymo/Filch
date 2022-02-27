package me.squeezymo.migrator.spotify.impl.domain

import com.google.gson.GsonBuilder
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import me.squeezymo.cache.api.CacheConfig
import me.squeezymo.cache.api.ICacheDataSource
import me.squeezymo.core.domain.data.*
import me.squeezymo.core.domain.repository.BaseRepository
import me.squeezymo.core.ext.serializeToMap
import me.squeezymo.migrator.spotify.api.SpotifyPlaylistID
import me.squeezymo.migrator.spotify.api.SpotifyTrackID
import me.squeezymo.migrator.spotify.api.domain.ISpotifyRepository
import me.squeezymo.migrator.spotify.api.domain.data.SpotifyPlaylist
import me.squeezymo.migrator.spotify.api.domain.data.SpotifyTrack
import me.squeezymo.migrator.spotify.impl.domain.cache.SpotifyProfileCache
import me.squeezymo.migrator.spotify.impl.domain.data.SearchTarget
import me.squeezymo.migrator.spotify.impl.domain.mapper.ISpotifyDtoToDomainMapper
import me.squeezymo.migrator.spotify.impl.network.SpotifyDataSource
import me.squeezymo.migrator.spotify.impl.network.SpotifyDataSourceFactory
import me.squeezymo.migrator.spotify.impl.network.SpotifyProfileCacheFactory
import me.squeezymo.migrator.spotify.impl.network.contract.SupportsPaging
import me.squeezymo.migrator.spotify.impl.network.dto.SpotifyApiErrorDTO
import me.squeezymo.migrator.spotify.impl.network.dto.SpotifyTrackItemDTO
import me.squeezymo.oauth.api.IOAuth2Broker
import me.squeezymo.oauth.api.exception.ApiException
import me.squeezymo.streamingservices.api.domain.data.StreamingService
import okhttp3.ResponseBody
import retrofit2.Response
import javax.inject.Inject

internal class SpotifyRepository @Inject constructor(
    dataSourceFactory: SpotifyDataSourceFactory,
    profileCacheFactory: SpotifyProfileCacheFactory,
    private val cacheDataSource: ICacheDataSource,
    oAuth2Broker: IOAuth2Broker,
    gsonBuilder: GsonBuilder,
    private val mapper: ISpotifyDtoToDomainMapper
) : BaseRepository(gsonBuilder), ISpotifyRepository {

    private val serviceId = StreamingService.SPOTIFY.id
    private val oauth2StateProvider = requireNotNull(oAuth2Broker.getOAuth2ServiceById(serviceId))
    private val dataSource: SpotifyDataSource by lazy {
        dataSourceFactory.create(oauth2StateProvider)
    }
    private val profileCache: SpotifyProfileCache by lazy {
        profileCacheFactory.create(dataSource)
    }
    private val cacheConfig = object : CacheConfig {
        override val serviceId = StreamingService.SPOTIFY.id
        override val idObjectPath = "track.id"
    }

    override suspend fun requestTracks(
        excludeExternalIds: Boolean
    ): List<EntityWithExternalIDs<SpotifyTrack>> {
        return withPaging(
            initial = mutableListOf(),
            nextResponse = { nextBatchUrl ->
                if (nextBatchUrl == null) dataSource.getTracks()
                else dataSource.getTracksByUrl(nextBatchUrl)
            },
            handleSuccessfulResponse = { tracks, response ->
                withContext(Dispatchers.IO) {
                    val entities = Array(response.items.size) { index ->
                        val trackItemDto = response.items[index]
                        val track = mapper.mapTrackItem(trackItemDto)

                        async {
                            EntityWithExternalIDs(
                                track,
                                if (excludeExternalIds) {
                                    mapOf(serviceId to trackItemDto.track.id)
                                } else {
                                    extractTrackIds(track)
                                }
                            )
                        }
                    }

                    awaitAll(*entities)
                }.let(tracks::addAll)

                tracks
            }
        )
    }

    override suspend fun saveTracks(
        ids: Set<SpotifyTrackID>
    ): Flow<Map<SpotifyTrackID, StatelessResult>> = flow {
        val savedTracks: MutableMap<SpotifyTrackID, StatelessResult> = HashMap()

        ids
            .chunked(SpotifyDataSource.MAX_TRACKS_TO_SAVE_PER_REQUEST)
            .forEach { idsChunk ->
                val response = dataSource.saveTracks(idsChunk)

                if (response.isSuccessful) {
                    idsChunk.forEach { id ->
                        savedTracks[id] = StatelessResult.Success
                    }
                } else {
                    idsChunk.forEach { id ->
                        savedTracks[id] = StatelessResult.Error
                    }
                }

                emit(HashMap(savedTracks))
            }
    }

    override suspend fun saveTracksToPlaylist(
        playlistId: SpotifyPlaylistID,
        ids: Set<SpotifyTrackID>
    ): Flow<Map<SpotifyTrackID, StatelessResult>> = flow {
        val savedTracks: MutableMap<SpotifyTrackID, StatelessResult> = HashMap()

        ids
            .chunked(SpotifyDataSource.MAX_TRACKS_TO_ADD_TO_PLAYLIST_PER_REQUEST)
            .forEach { idsChunk ->
                val response = dataSource.saveTracksToPlaylist(playlistId, idsChunk)

                if (response.isSuccessful) {
                    idsChunk.forEach { id ->
                        savedTracks[id] = StatelessResult.Success
                    }
                } else {
                    idsChunk.forEach { id ->
                        savedTracks[id] = StatelessResult.Error
                    }
                }

                emit(HashMap(savedTracks))
            }
    }

    override suspend fun createPlaylist(
        playlistTitle: PlaylistTitle
    ): SpotifyPlaylistID {
        val playlistResponse = dataSource.createPlaylist(
            profileCache.getProfile(
                serviceId,
                oauth2StateProvider.getFreshAccessState()?.accessToken,
                ::handleError
            ).id,
            playlistTitle
        )

        if (playlistResponse.isSuccessful) {
            return playlistResponse.body()!!.id
        }
        else {
            handleError(playlistResponse.errorBody()!!)
        }
    }

    override suspend fun requestPlaylists(): List<SpotifyPlaylist> {
        return withPaging(
            initial = mutableListOf(),
            nextResponse = { nextBatchUrl ->
                if (nextBatchUrl == null) dataSource.getPlaylists()
                else dataSource.getPlaylistsByUrl(nextBatchUrl)
            },
            handleSuccessfulResponse = { playlists, response ->
                response.items.forEach { spotifyPlaylistDTO ->
                    playlists.add(
                        mapper.mapPlaylist(
                            spotifyPlaylistDTO,
                            requestsTracks(spotifyPlaylistDTO.id)
                        )
                    )
                }
                playlists
            }
        )
    }

    private suspend fun requestsTracks(playlistId: SpotifyPlaylistID): List<SpotifyTrackItemDTO> {
        return withPaging(
            initial = mutableListOf(),
            nextResponse = { nextBatchUrl ->
                if (nextBatchUrl == null) dataSource.getTracks(playlistId)
                else dataSource.getTracksByUrl(nextBatchUrl)
            },
            handleSuccessfulResponse = { tracks, response ->
                tracks.addAll(response.items)
                tracks
            }
        )
    }

    override suspend fun findTrack(
        track: String,
        artist: String?,
        album: String?,
        year: Int?,
        externalIds: Map<StreamingServiceID, ID>
    ): EntityWithExternalIDs<SpotifyTrack>? {
        val response = dataSource.search(
            targets = listOf(SearchTarget.TRACK),
            track = track,
            artist = artist,
            album = album,
            year = year
        )

        return if (response.isSuccessful) {
            response
                .body()!!
                .tracks
                ?.items
                ?.firstOrNull()
                ?.also { dto ->
                    withContext(Dispatchers.IO) {
                        launch {
                            runCatching {
                                val record = HashMap<String, Any>(externalIds).also { record ->
                                    record["track"] = dto.serializeToMap()
                                }
                                cacheDataSource.addOrUpdateRecord(
                                    cacheConfig,
                                    dto.id,
                                    record
                                )
                            }
                        }
                    }
                }
                ?.let { spotifyTrackDto ->
                    val spotifyTrack = mapper.mapTrack(spotifyTrackDto)
                    EntityWithExternalIDs(
                        spotifyTrack,
                        HashMap(externalIds).apply {
                            put(serviceId, spotifyTrack.id)
                        }
                    )
                }
        } else {
            handleError(response.errorBody())
        }
    }

    override suspend fun addExternalTrackId(
        trackId: SpotifyTrackID,
        externalIds: Map<StreamingServiceID, ID>
    ) {
        cacheDataSource
            .addOrUpdateRecord(
                cacheConfig,
                recordId = trackId,
                record = externalIds
            )
    }

    override suspend fun extractTrackIds(
        track: SpotifyTrack,
        excludeExternalTrackIds: Boolean
    ): Map<StreamingServiceID, ID> {
        return extractIds(
            serviceId,
            track.id,
            if (excludeExternalTrackIds) {
                null
            } else {
                cacheDataSource.findRecordById(
                    cacheConfig,
                    recordId = track.id
                )
            }
        )
    }

    private suspend fun <IN, OUT> withPaging(
        initial: OUT,
        nextResponse: suspend (nextBatchUrl: String?) -> Response<IN>,
        handleSuccessfulResponse: suspend (acc: OUT, IN) -> OUT
    ): OUT where IN : SupportsPaging {
        var nextBatchUrl: String? = null
        var result = initial

        do {
            val response = nextResponse(nextBatchUrl)

            if (response.isSuccessful) {
                val body = response.body()!!

                result = handleSuccessfulResponse(result, body)
                nextBatchUrl = body.nextBatchUrl
            } else {
                handleError(response.errorBody())
            }
        } while (nextBatchUrl != null)

        return result
    }

    private fun handleError(errorBody: ResponseBody?): Nothing {
        val error = errorBody?.deserializeAsJsonError<SpotifyApiErrorDTO>()!!
        throw ApiException(serviceId, error.status, error.message)
    }

}
