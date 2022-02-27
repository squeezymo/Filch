package me.squeezymo.usercontent.impl.ui

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import me.squeezymo.core.domain.data.BasePlaylist
import me.squeezymo.core.domain.data.BaseTrack
import me.squeezymo.core.domain.data.EntityWithExternalIDs
import me.squeezymo.core.domain.data.ID
import me.squeezymo.core.ext.*
import me.squeezymo.core.ui.BaseViewModel
import me.squeezymo.core.ui.IBaseViewModel
import me.squeezymo.core.ui.vmdelegate.IErrorHandlingVmDelegate
import me.squeezymo.streamingservices.api.domain.data.StreamingService
import me.squeezymo.usercontent.api.domain.usecase.IAddTracksToPlaylistUC
import me.squeezymo.usercontent.api.domain.usecase.ICreatePlaylistUC
import me.squeezymo.usercontent.impl.data.UserContentError
import me.squeezymo.usercontent.impl.domain.cache.IPlaylistCache
import me.squeezymo.usercontent.impl.domain.data.CompositeTrackId
import me.squeezymo.usercontent.impl.ui.data.*
import me.squeezymo.usercontent.impl.ui.event.PlaylistToUserContentResult
import me.squeezymo.usercontent.impl.ui.mapper.TrackUiMapper
import me.squeezymo.usercontent.impl.ui.mapper.UserContentUiMapper
import me.squeezymo.usercontent.impl.ui.uistate.PlayerUiState
import me.squeezymo.usercontent.impl.ui.uistate.UserContentUiState
import me.squeezymo.usercontent.impl.ui.utils.VmMigrationUtils
import me.squeezymo.usercontent.impl.ui.vmdelegate.IPlayerVmDelegate
import javax.inject.Inject

internal interface IPlaylistViewModel :
    IBaseViewModel, IErrorHandlingVmDelegate<UserContentError>, IPlayerVmDelegate {

    val fromService: StreamingService

    val toService: StreamingService

    val playlistTitle: String

    val uiState: StateFlow<UserContentUiState>

    fun toggleSelectionMode()

    fun toggleShowMigratedTracks()

    fun migrate()

    fun setResultCallback(
        callback: (PlaylistToUserContentResult) -> Unit
    )

}

@HiltViewModel
internal class PlaylistViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val playlistCache: IPlaylistCache,
    private val createPlaylistUC: ICreatePlaylistUC,
    private val addTracksToPlaylistUC: IAddTracksToPlaylistUC,
    trackUiMapper: TrackUiMapper,
    errorHandlingDelegate: IErrorHandlingVmDelegate<UserContentError>
) : BaseViewModel(savedStateHandle), IPlaylistViewModel,
    IErrorHandlingVmDelegate<UserContentError> by errorHandlingDelegate {

    private val args = PlaylistFragmentArgs.fromBundle(savedStateHandle.toBundle())

    override val fromService = StreamingService.requireById(args.from)
    override val toService = StreamingService.requireById(args.to)

    override val playlistTitle = args.title
    private var srcPlaylist =
        requireNotNull(playlistCache.getPlaylist(fromService, playlistTitle)) {
            "Fetching the playlist from network is not supported yet. Playlist must be saved " +
                    "to cache prior to creating ${PlaylistFragment::class.java.canonicalName}"
        }

    private var dstPlaylist: BasePlaylist? =
        playlistCache.getPlaylist(toService, playlistTitle)

    private val migrationUtils = VmMigrationUtils(toService)
    private val userContentUiMapper = UserContentUiMapper(trackUiMapper)
    private var resultCallback: (PlaylistToUserContentResult) -> Unit = {
        // TODO Replace with Timber
        Log.w(PlaylistViewModel::class.simpleName, "Result callback is not set. Result ignored")
    }

    private val isInSelectionMode: MutableStateFlow<Boolean> =
        MutableStateFlow(false)
    private val showMigratedTracks: MutableStateFlow<Boolean> =
        MutableStateFlow(true)
    private val tracksSelectedForMigration: MutableStateFlow<Set<ID>> =
        MutableStateFlow(emptySet())
    private val tracksMigrationState: MutableStateFlow<Map<CompositeTrackId, TrackMigrationState>> =
        MutableStateFlow(emptyMap())
    private val playerState: MutableStateFlow<PlayerState?> =
        MutableStateFlow(null)

    override val uiState: MutableStateFlow<UserContentUiState> =
        MutableStateFlow(UserContentUiState.loading())
    override val playerUiState: MutableStateFlow<PlayerUiState> =
        MutableStateFlow(PlayerUiState.None)

    private val tracksSearchState = TracksSearchState(
        progress = 100,
        inProgress = false,
        srcIdToTrack = srcPlaylist.tracks.associateBy { track ->
            requireNotNull(track.ids[fromService.id])
        },
        dstIdToTrack = dstPlaylist?.tracks?.associateBy { track ->
            requireNotNull(track.ids[toService.id])
        } ?: emptyMap(),
        srcIdToDstId = srcPlaylist.tracks.associate { track ->
            requireNotNull(track.ids[fromService.id]) to track.ids[toService.id]
        }.filterNotNullValues()
    )

    private val migratedTrackIds: List<ID>
        get() = tracksMigrationState
            .value
            .filterValues { state ->
                state is TrackMigrationState.Migrated
            }
            .keys
            .map {
                it.srcTrackId
            }

    init {
        viewModelScope.launch {
            launch(Dispatchers.Default) {
                combineToTuple(
                    isInSelectionMode,
                    tracksSelectedForMigration,
                    tracksMigrationState.map { tracksMigrationState ->
                        tracksMigrationState.mapKeys {
                            it.key.srcTrackId
                        }
                    },
                    showMigratedTracks
                ).map { (isInSelectionMode,
                            tracksSelectedForMigration,
                            tracksMigrationState,
                            showMigratedTracks) ->

                    userContentUiMapper.createUiState(
                        tracksUiData = TracksUiData.Show(
                            tracksSearchState = tracksSearchState,
                            tracksMigrationState = tracksMigrationState,
                            tracksSelectedForMigration = tracksSelectedForMigration
                        ),
                        playlistsUiData = PlaylistsUiData.Hide,
                        isInSelectionMode = isInSelectionMode,
                        showMigratedTracks = showMigratedTracks,
                        dstService = toService
                    )
                }.collectTo(uiState)
            }

            launch(Dispatchers.Default) {
                combineToTuple(
                    playerState,
                    isInSelectionMode,
                    tracksSelectedForMigration
                ).map { (playerState,
                            isInSelectionMode,
                            tracksSelectedForMigration) ->
                    if (playerState == null) {
                        PlayerUiState.None
                    } else {
                        PlayerUiState.Player(
                            trackId = playerState.id,
                            url = playerState.url,
                            track = playerState.track,
                            artist = playerState.artist,
                            isInSelectionMode = isInSelectionMode,
                            dstService = toService,
                            migrationStatusUi = userContentUiMapper.createMigrationStatusUi(
                                srcId = playerState.id,
                                tracksSearchState = tracksSearchState,
                                tracksMigrationState = emptyMap(),
                                tracksSelectedForMigration = tracksSelectedForMigration
                            )
                        )
                    }
                }.collectTo(playerUiState)
            }

            launch(Dispatchers.Default) {
                tracksMigrationState
                    .collect { tracksMigrationState ->
                        tracksSelectedForMigration.updateSet { ids ->
                            ids.removeAll(
                                tracksMigrationState
                                    .mapKeys {
                                        it.key.srcTrackId
                                    }
                                    .filter { (_, state) ->
                                        state !is TrackMigrationState.Error
                                    }
                                    .keys
                            )
                        }
                    }
            }
        }
    }

    override fun toggleSelectionMode() {
        val isInSelectionMode = this.isInSelectionMode.value
        this.isInSelectionMode.value = !isInSelectionMode

        if (!isInSelectionMode) {
            tracksSelectedForMigration.value = emptySet()
        }
    }

    override fun toggleShowMigratedTracks() {
        showMigratedTracks.update { !it }
    }

    override fun selectTrackForMigration(id: ID, isChecked: Boolean) {
        tracksSelectedForMigration.updateSet { ids ->
            if (isChecked) ids.add(id) else ids.remove(id)
        }
    }

    override fun migrate() {
        viewModelScope.launch {
            launch {
                migrateTracks()
            }
        }
    }

    override fun setResultCallback(
        callback: (PlaylistToUserContentResult) -> Unit
    ) {
        this.resultCallback = callback
    }

    private suspend fun migrateTracks() {
        val srcIdToDstId = playlistCache.getSrcIdToDstIdTrackMapping(fromService, toService)

        val trackIds = if (isInSelectionMode.value) {
            tracksSelectedForMigration.value
        } else {
            srcPlaylist
                .tracks
                .mapNotNull {
                    it.ids[fromService.id]
                }
                .filter { id ->
                    id !in migratedTrackIds && srcIdToDstId?.get(id) != null
                }
                .toSet()
        }

        withContext(Dispatchers.Default) {
            launch {
                migrationUtils
                    .migrateTracksToPlaylist(
                        createPlaylistUC,
                        addTracksToPlaylistUC,
                        playlistTitle,
                        dstPlaylist?.id,
                        trackIds,
                        srcIdToDstId ?: emptyMap()
                    )
                    .map { playlistTracksMigrationState ->
                        val newDstPlaylist = dstPlaylist
                            ?: BasePlaylist(
                                id = playlistTracksMigrationState.playlistId,
                                title = playlistTitle,
                                tracks = emptyList(),
                                thumbnailUrl = srcPlaylist.thumbnailUrl
                            )

                        val newTracksMigrationState = tracksMigrationState.value
                            .toMutableMap()
                            .also { map: MutableMap<CompositeTrackId, TrackMigrationState> ->
                                map.putAll(
                                    playlistTracksMigrationState
                                        .tracksMigrationState
                                )

                                dstPlaylist = newDstPlaylist.copy(
                                    id = playlistTracksMigrationState.playlistId,
                                    tracks = mergeDstTracks(
                                        newDstPlaylist.tracks,
                                        map.filterValues { it is TrackMigrationState.Migrated }.keys
                                    )
                                )
                            }

                        newTracksMigrationState
                    }
                    .collectTo(tracksMigrationState)
            }
        }
    }

    private fun mergeDstTracks(
        originalTracks: List<EntityWithExternalIDs<BaseTrack>>,
        migratedTrackIds: Set<CompositeTrackId>
    ): List<EntityWithExternalIDs<BaseTrack>> {
        val migratedTracks = migratedTrackIds.mapNotNull { compositeId ->
            if (compositeId.dstTrackId == null) {
                return@mapNotNull null
            }

            val srcTrack = srcPlaylist.tracks.find { srcTrack: EntityWithExternalIDs<BaseTrack> ->
                srcTrack.ids[fromService.id] == compositeId.srcTrackId
            }

            if (srcTrack != null) {
                srcPlaylist = srcPlaylist.copy(
                    tracks = srcPlaylist.tracks.map { track: EntityWithExternalIDs<BaseTrack> ->
                        if (srcTrack === track) {
                            srcTrack.copy(
                                ids = srcTrack.ids.toMutableMap().also {
                                    it[toService.id] = compositeId.dstTrackId
                                }
                            )
                        } else {
                            track
                        }
                    }
                )
            }

            dstPlaylist?.tracks?.find { dstTrack: EntityWithExternalIDs<BaseTrack> ->
                dstTrack.ids[toService.id] == compositeId.dstTrackId
            } ?: srcTrack?.copy(
                ids = srcTrack.ids.toMutableMap().also {
                    it[toService.id] = compositeId.dstTrackId
                }
            )
        }

        return originalTracks + migratedTracks
    }

    override fun startAudioPreview(id: ID) {
        val track = srcPlaylist.tracks.find { it.ids[fromService.id] == id }
        val audioPreviewUrl = track?.entity?.audioPreviewUrl

        if (track != null && audioPreviewUrl != null) {
            playerState.value = PlayerState(
                id = id,
                url = audioPreviewUrl,
                track = track.entity.title,
                artist = track.entity.artist
            )
        }
    }

    override fun startNextAudioPreview() {
        val currentId = playerState.value?.id ?: return
        val ids = srcPlaylist.tracks.filter { track ->
            track.entity.audioPreviewUrl != null
        }.mapNotNull { it.ids[fromService.id] }
        val nextId = ids.getOrNull(ids.indexOf(currentId) + 1)
            ?: ids.getOrNull(0)
            ?: return

        startAudioPreview(nextId)
    }

    override fun notifyOnPlayerDismissed() {
        playerState.value = null
    }

    override fun onCleared() {
        playlistCache.clearPlaylist(fromService, srcPlaylist.title)
        playlistCache.clearPlaylist(toService, srcPlaylist.title)

        val srcIdToDstId = srcPlaylist.tracks.associate { track ->
            requireNotNull(track.ids[fromService.id]) to track.ids[toService.id]
        }.filterNotNullValues()

        val allMigratedTracks = migratedTrackIds + srcPlaylist.tracks.mapNotNull { srcTrack ->
            val srcId = srcTrack.ids[fromService.id]
            val isMigrated = tracksSearchState
                .dstIdToTrack
                .containsKey(tracksSearchState.srcIdToDstId[srcId])

            if (isMigrated) srcId else null
        }

        resultCallback(
            PlaylistToUserContentResult(
                srcPlaylist = srcPlaylist,
                dstPlaylist = dstPlaylist,
                dstPlaylistMigrationState = when {
                    srcIdToDstId.size == allMigratedTracks.size ->
                        PlaylistMigrationState.Migrated
                    allMigratedTracks.isNotEmpty() ->
                        PlaylistMigrationState.PartiallyMigrated
                    else ->
                        null
                }
            )
        )
    }

}
