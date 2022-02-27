package me.squeezymo.migrator.deezer.impl.domain

import com.google.gson.GsonBuilder
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import me.squeezymo.cache.api.CacheConfig
import me.squeezymo.cache.api.ICacheDataSource
import me.squeezymo.core.domain.data.*
import me.squeezymo.core.domain.repository.BaseRepository
import me.squeezymo.core.ext.serializeToMap
import me.squeezymo.migrator.deezer.api.DeezerPlaylistID
import me.squeezymo.migrator.deezer.api.DeezerTrackID
import me.squeezymo.migrator.deezer.api.domain.IDeezerRepository
import me.squeezymo.migrator.deezer.api.domain.data.DeezerAlbum
import me.squeezymo.migrator.deezer.api.domain.data.DeezerArtist
import me.squeezymo.migrator.deezer.api.domain.data.DeezerPlaylist
import me.squeezymo.migrator.deezer.api.domain.data.DeezerTrack
import me.squeezymo.migrator.deezer.impl.domain.mapper.IDeezerDtoToDomainMapper
import me.squeezymo.migrator.deezer.impl.network.DeezerDataSource
import me.squeezymo.migrator.deezer.impl.network.DeezerDataSourceFactory
import me.squeezymo.migrator.deezer.impl.network.query.DeezerPaginableData
import me.squeezymo.oauth.api.IOAuth2Broker
import me.squeezymo.streamingservices.api.domain.data.StreamingService
import okhttp3.ResponseBody
import retrofit2.Response
import java.util.*
import javax.inject.Inject
import kotlin.collections.HashMap

internal class DeezerRepository @Inject constructor(
    dataSourceFactory: DeezerDataSourceFactory,
    private val cacheDataSource: ICacheDataSource,
    oAuth2Broker: IOAuth2Broker,
    gsonBuilder: GsonBuilder,
    private val mapper: IDeezerDtoToDomainMapper
) : BaseRepository(gsonBuilder), IDeezerRepository {

    private val serviceId = StreamingService.DEEZER.id
    private val dataSource: DeezerDataSource by lazy {
        dataSourceFactory.create(
            requireNotNull(oAuth2Broker.getOAuth2ServiceById(serviceId))
        )
    }
    private val cacheConfig = object : CacheConfig {
        override val serviceId = StreamingService.DEEZER.id
        override val idObjectPath = "track.id"
    }

    override suspend fun requestArtists(): List<DeezerArtist> {
        return withPaging(
            initial = mutableListOf(),
            nextResponse = { nextBatchUrl ->
                if (nextBatchUrl == null) dataSource.getArtists()
                else dataSource.getArtistsByUrl(nextBatchUrl)
            },
            handleSuccessfulResponse = { artists, artistDtos ->
                artists.addAll(artistDtos.map(mapper::mapArtist))
                artists
            }
        )
    }

    override suspend fun requestAlbums(): List<DeezerAlbum> {
        return withPaging(
            initial = mutableListOf(),
            nextResponse = { nextBatchUrl ->
                if (nextBatchUrl == null) dataSource.getAlbums()
                else dataSource.getAlbumsByUrl(nextBatchUrl)
            },
            handleSuccessfulResponse = { albums, albumDtos ->
                albums.addAll(albumDtos.map(mapper::mapAlbum))
                albums
            }
        )
    }

    override suspend fun requestTracks(
        excludeExternalIds: Boolean
    ): List<EntityWithExternalIDs<DeezerTrack>> {
        return withPaging(
            initial = mutableListOf(),
            nextResponse = { nextBatchUrl ->
                if (nextBatchUrl == null) dataSource.getTracks()
                else dataSource.getTracksByUrl(nextBatchUrl)
            },
            handleSuccessfulResponse = { tracks, trackDtos ->
                withContext(Dispatchers.IO) {
                    val entities = Array(trackDtos.size) { index ->
                        val trackDto = trackDtos[index]
                        val track = mapper.mapTrack(trackDto)

                        async {
                            EntityWithExternalIDs(
                                track,
                                if (excludeExternalIds) {
                                    mapOf(serviceId to track.id)
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
        ids: Set<DeezerTrackID>
    ): Flow<Map<DeezerTrackID, StatelessResult>> = channelFlow  {
        val savedTracks: MutableMap<DeezerTrackID, StatelessResult> =
            Collections.synchronizedMap(HashMap())

        withContext(Dispatchers.IO) {
            val tasks = ids.map { id ->
                async {
                    try {
                        val response = dataSource.addTrackToFavorites(id)

                        if (response.isSuccessful && response.body().toString() == "true") {
                            savedTracks[id] = StatelessResult.Success
                        }
                        else {
                            savedTracks[id] = StatelessResult.Error
                        }
                    }
                    catch (e: Exception) {
                        savedTracks[id] = StatelessResult.Error
                    }

                    send(HashMap(savedTracks))
                }
            }

            tasks.awaitAll()
        }
    }

    override suspend fun saveTracksToPlaylist(
        playlistId: DeezerPlaylistID,
        ids: Set<DeezerTrackID>
    ): Flow<Map<DeezerTrackID, StatelessResult>> {
        TODO("Not yet implemented")
    }

    override suspend fun createPlaylist(
        playlistTitle: PlaylistTitle
    ): DeezerPlaylistID {
        TODO("Not yet implemented")
    }

    override suspend fun requestPlaylists(): List<DeezerPlaylist> {
        return withPaging(
            initial = mutableListOf(),
            nextResponse = { nextBatchUrl ->
                if (nextBatchUrl == null) dataSource.getPlaylists()
                else dataSource.getPlaylistsByUrl(nextBatchUrl)
            },
            handleSuccessfulResponse = { playlists, playlistShortDtos ->
                playlistShortDtos.forEach { deezerPlaylistShortDto ->
                    val playlistFullDtoResponse = dataSource.getPlaylist(deezerPlaylistShortDto.id)

                    if (playlistFullDtoResponse.isSuccessful) {
                        // TODO Make sure "tracks" cannot contain "next" url
                        val body = playlistFullDtoResponse.body()!!

                        playlists.add(
                            mapper.mapPlaylist(
                                body,
                                body.tracks.data
                            )
                        )
                    } else {
                        handleError(playlistFullDtoResponse.errorBody())
                    }

                }
                playlists
            }
        )
    }

    override suspend fun findTrack(
        track: String,
        artist: String?,
        album: String?,
        externalIds: Map<StreamingServiceID, ID>
    ): EntityWithExternalIDs<DeezerTrack>? {
        return dataSource.search(
            track = track,
            artist = artist,
            album = album
        ).let { response ->
            if (response.isSuccessful) {
                response
                    .body()!!
                    .data
                    .firstOrNull()
                    ?.also { dto ->
                        withContext(Dispatchers.IO) {
                            launch {
                                runCatching {
                                    val record = HashMap<String, Any>(externalIds).also { record ->
                                        record["track"] = dto.serializeToMap()
                                    }
                                    cacheDataSource.addOrUpdateRecord(cacheConfig, dto.id, record)
                                }
                            }
                        }
                    }
                    ?.let { deezerTrackDto ->
                        val deezerTrack = mapper.mapTrack(deezerTrackDto)
                        EntityWithExternalIDs(
                            deezerTrack,
                            HashMap(externalIds).apply {
                                put(serviceId, deezerTrack.id)
                            }
                        )
                    }
            } else {
                handleError(response.errorBody())
            }
        }
    }

    override suspend fun addExternalTrackId(
        trackId: DeezerTrackID,
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
        track: DeezerTrack,
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

    private suspend fun <IN : Any, OUT> withPaging(
        initial: OUT,
        nextResponse: suspend (nextBatchUrl: String?) -> Response<DeezerPaginableData<IN>>,
        handleSuccessfulResponse: suspend (acc: OUT, List<IN>) -> OUT
    ): OUT {
        var nextBatchUrl: String? = null
        var result = initial

        do {
            val response = nextResponse(nextBatchUrl)

            if (response.isSuccessful) {
                val body = response.body()!!

                result = handleSuccessfulResponse(result, body.data)
                nextBatchUrl = body.next
            } else {
                handleError(response.errorBody())
            }
        } while (nextBatchUrl != null)

        return result
    }

    private fun handleError(error: ResponseBody?): Nothing {
        // TODO
        throw RuntimeException()
    }

}
