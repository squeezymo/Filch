package me.squeezymo.usercontent.impl.ui

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import me.squeezymo.core.domain.data.*
import me.squeezymo.core.ext.*
import me.squeezymo.core.ui.BaseViewModel
import me.squeezymo.core.ui.IBaseViewModel
import me.squeezymo.core.ui.vmdelegate.IErrorHandlingVmDelegate
import me.squeezymo.streamingservices.api.domain.data.StreamingService
import me.squeezymo.usercontent.api.domain.usecase.*
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
import me.squeezymo.usercontent.impl.ui.utils.VmSearchUtils
import me.squeezymo.usercontent.impl.ui.vmdelegate.IPlayerVmDelegate
import javax.inject.Inject

internal interface IUserContentViewModel :
    IBaseViewModel, IErrorHandlingVmDelegate<UserContentError>, IPlayerVmDelegate {

    val fromService: StreamingService

    val toService: StreamingService

    val uiState: StateFlow<UserContentUiState>

    fun load(forceRefresh: Boolean = false)

    fun toggleSelectionMode()

    fun toggleShowMigratedTracks()

    fun selectPlaylist(title: PlaylistTitle)

    fun selectPlaylistForMigration(title: PlaylistTitle, isChecked: Boolean)

    fun migrate()

    fun handlePlaylistToUserContentResult(result: PlaylistToUserContentResult)

}

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
internal class UserContentViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val getTracksUC: IGetTracksUC,
    private val getPlaylistsUC: IGetPlaylistsUC,
    private val findTrackIdUC: IFindTrackIdUC,
    private val addTracksToLibraryUC: IAddTracksToLibraryUC,
    private val createPlaylistUC: ICreatePlaylistUC,
    private val addTracksToPlaylistUC: IAddTracksToPlaylistUC,
    private val playlistCache: IPlaylistCache,
    trackUiMapper: TrackUiMapper,
    errorHandlingDelegate: IErrorHandlingVmDelegate<UserContentError>
) : BaseViewModel(savedStateHandle),
    IUserContentViewModel,
    IErrorHandlingVmDelegate<UserContentError> by errorHandlingDelegate {

    private val args = UserContentFragmentArgs.fromBundle(savedStateHandle.toBundle())

    override val fromService = StreamingService.requireById(args.from)
    override val toService = StreamingService.requireById(args.to)

    private val searchUtils = VmSearchUtils(fromService, toService)
    private val migrationUtils = VmMigrationUtils(toService)
    private val userContentUiMapper = UserContentUiMapper(trackUiMapper)

    private val srcIdToTrack: MutableStateFlow<Map<ID, EntityWithExternalIDs<BaseTrack>>?> =
        MutableStateFlow(null)
    private val dstIdToTrack: MutableStateFlow<Map<ID, EntityWithExternalIDs<BaseTrack>>?> =
        MutableStateFlow(null)
    private val srcIdToDstId: MutableStateFlow<Map<ID, ID>?> =
        MutableStateFlow(null)
    private val srcIdToDstIdWithinPlaylist: MutableStateFlow<Map<ID, ID>?> =
        MutableStateFlow(null)
    private val srcTitleToPlaylist: MutableStateFlow<Map<PlaylistTitle, BasePlaylist>?> =
        MutableStateFlow(null)
    private val dstTitleToPlaylist: MutableStateFlow<Map<PlaylistTitle, BasePlaylist>?> =
        MutableStateFlow(null)
    private val isInSelectionMode: MutableStateFlow<Boolean> =
        MutableStateFlow(false)
    private val showMigratedTracks: MutableStateFlow<Boolean> =
        MutableStateFlow(true)
    private val tracksSelectedForMigration: MutableStateFlow<Set<ID>> =
        MutableStateFlow(emptySet())
    private val tracksMigrationState: MutableStateFlow<Map<CompositeTrackId, TrackMigrationState>> =
        MutableStateFlow(emptyMap())
    private val playlistsSelectedForMigration: MutableStateFlow<Set<PlaylistTitle>> =
        MutableStateFlow(emptySet())
    private val playlistsMigrationState: MutableStateFlow<Map<ID, PlaylistMigrationState>> =
        MutableStateFlow(emptyMap())
    private val playerState: MutableStateFlow<PlayerState?> =
        MutableStateFlow(null)

    override val uiState: MutableStateFlow<UserContentUiState> =
        MutableStateFlow(UserContentUiState.loading())
    override val playerUiState: MutableStateFlow<PlayerUiState> =
        MutableStateFlow(PlayerUiState.None)

    init {
        val tracksSearchState: MutableStateFlow<TracksSearchState> =
            MutableStateFlow(
                TracksSearchState(0, true, emptyMap(), emptyMap(), emptyMap())
            )
        val playlistsSearchState: MutableStateFlow<PlaylistsSearchState> =
            MutableStateFlow(
                PlaylistsSearchState(true, emptyList(), emptyMap(), emptyMap(), emptyMap())
            )

        viewModelScope.launch {
            launch(Dispatchers.Default) {
                zipToTuple(
                    srcIdToTrack,
                    dstIdToTrack
                ).flatMapLatest { (srcIdToTrack, dstIdToTrack) ->
                    if (srcIdToTrack == null || dstIdToTrack == null) {
                        flowOf(
                            TracksSearchState(0, true, emptyMap(), emptyMap(), emptyMap())
                        )
                    } else {
                        searchUtils.searchForTracks(findTrackIdUC, srcIdToTrack, dstIdToTrack)
                    }
                }.collectTo(tracksSearchState)
            }

            launch(Dispatchers.Default) {
                tracksSearchState
                    .map {
                        it.srcIdToDstId
                    }
                    .collectTo(srcIdToDstId)
            }

            launch(Dispatchers.Default) {
                playlistsSearchState
                    .map {
                        it.srcIdToDstIdWithinPlaylist
                    }
                    .collect { srcIdToDstIdTrackMapping ->
                        srcIdToDstIdWithinPlaylist.value = srcIdToDstIdTrackMapping
                        playlistCache.setTrackMapping(
                            fromService,
                            toService,
                            srcIdToDstIdTrackMapping
                        )
                    }
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

            launch(Dispatchers.Default) {
                playlistsMigrationState
                    .collect { playlistsMigrationState ->
                        playlistsSelectedForMigration.updateSet { ids ->
                            ids.removeAll(
                                playlistsMigrationState
                                    .filter { (_, state) ->
                                        state !is PlaylistMigrationState.Error
                                    }
                                    .keys
                            )
                        }
                    }
            }

            launch(Dispatchers.Default) {
                zipToTuple(
                    srcTitleToPlaylist,
                    dstTitleToPlaylist
                ).flatMapLatest { (srcTitleToPlaylist, dstTitleToPlaylist) ->
                    if (srcTitleToPlaylist == null || dstTitleToPlaylist == null) {
                        flowOf(
                            PlaylistsSearchState(
                                true,
                                emptyList(),
                                emptyMap(),
                                emptyMap(),
                                emptyMap()
                            )
                        )
                    } else {
                        searchUtils.searchForPlaylists(
                            findTrackIdUC,
                            srcTitleToPlaylist,
                            dstTitleToPlaylist
                        )
                    }
                }.collectTo(playlistsSearchState)
            }

            launch(Dispatchers.Default) {
                combineToTuple(
                    tracksSearchState,
                    playlistsSearchState,
                    isInSelectionMode,
                    tracksSelectedForMigration,
                    tracksMigrationState.map { tracksMigrationState ->
                        tracksMigrationState.mapKeys {
                            it.key.srcTrackId
                        }
                    },
                    playlistsSelectedForMigration,
                    playlistsMigrationState,
                    showMigratedTracks
                ).map { (tracksSearchState,
                            playlistsSearchState,
                            isInSelectionMode,
                            tracksSelectedForMigration,
                            tracksMigrationState,
                            playlistsSelectedForMigration,
                            playlistsMigrationState,
                            showMigratedTracks) ->

                    userContentUiMapper.createUiState(
                        tracksUiData = TracksUiData.Show(
                            tracksSearchState = tracksSearchState,
                            tracksMigrationState = tracksMigrationState,
                            tracksSelectedForMigration = tracksSelectedForMigration
                        ),
                        playlistsUiData = PlaylistsUiData.Show(
                            playlistsSearchState = playlistsSearchState,
                            playlistsMigrationState = playlistsMigrationState,
                            playlistsSelectedForMigration = playlistsSelectedForMigration
                        ),
                        isInSelectionMode = isInSelectionMode,
                        showMigratedTracks = showMigratedTracks,
                        dstService = toService
                    )
                }.collectTo(uiState)
            }

            launch(Dispatchers.Default) {
                combineToTuple(
                    playerState,
                    tracksSearchState,
                    isInSelectionMode,
                    tracksMigrationState.map { tracksMigrationState ->
                        tracksMigrationState.mapKeys {
                            it.key.srcTrackId
                        }
                    },
                    tracksSelectedForMigration
                ).map { (playerState,
                            tracksSearchState,
                            isInSelectionMode,
                            tracksMigrationState,
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
                                tracksMigrationState = tracksMigrationState,
                                tracksSelectedForMigration = tracksSelectedForMigration
                            )
                        )
                    }
                }.collectTo(playerUiState)
            }
        }

        load()
    }

    override fun load(forceRefresh: Boolean) {
        viewModelScope.launch {
            tracksSelectedForMigration.value = emptySet()
            playlistsSelectedForMigration.value = emptySet()

            launch {
                try {
                    val idToTrack = reloadTracks()

                    srcIdToTrack.value = idToTrack.srcIdToTrack
                    dstIdToTrack.value = idToTrack.dstIdToTrack
                } catch (e: Exception) {
                    Log.w(
                        UserContentViewModel::class.java.simpleName,
                        "Error",
                        e
                    ) // TODO Replace with Timber
                    showError(UserContentError.TracksNotRetrieved)
                }
            }

            launch {
                runCatching {
                    val titleToPlaylist = reloadPlaylists()

                    srcTitleToPlaylist.value = titleToPlaylist.srcTitleToPlaylist
                    dstTitleToPlaylist.value = titleToPlaylist.dstTitleToPlaylist
                }
            }
        }
    }

    private suspend fun reloadTracks(): IdToTrack {
        return withContext(Dispatchers.IO) {
            srcIdToTrack.value = null
            dstIdToTrack.value = null
            srcIdToDstId.value = null

            val srcIdToTrack = async {
                val srcServiceId = fromService.id

                getTracksUC
                    .getTracks(srcServiceId)
                    .associateBy { track ->
                        requireNotNull(track.ids[srcServiceId])
                    }
            }

            val dstIdToTrack = async {
                val dstServiceId = toService.id

                getTracksUC
                    .getTracks(dstServiceId, excludeExternalIds = true)
                    .associateBy { track ->
                        requireNotNull(track.ids[dstServiceId])
                    }
            }

            IdToTrack(srcIdToTrack.await(), dstIdToTrack.await())
        }
    }

    private suspend fun reloadPlaylists(): TitleToPlaylist {
        return withContext(Dispatchers.IO) {
            srcTitleToPlaylist.value = null
            dstTitleToPlaylist.value = null

            val srcTitleToPlaylist = async {
                val srcServiceId = fromService.id

                getPlaylistsUC
                    .getPlaylists(srcServiceId)
                    .associateBy(BasePlaylist::title)
            }

            val dstTitleToPlaylist = async {
                val dstServiceId = toService.id

                getPlaylistsUC
                    .getPlaylists(dstServiceId, excludeExternalTrackIds = true)
                    .associateBy(BasePlaylist::title)
            }

            TitleToPlaylist(srcTitleToPlaylist.await(), dstTitleToPlaylist.await())
        }
    }

    override fun toggleSelectionMode() {
        val isInSelectionMode = this.isInSelectionMode.value
        this.isInSelectionMode.value = !isInSelectionMode

        if (!isInSelectionMode) {
            tracksSelectedForMigration.value = emptySet()
            playlistsSelectedForMigration.value = emptySet()
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

    override fun selectPlaylistForMigration(title: PlaylistTitle, isChecked: Boolean) {
        playlistsSelectedForMigration.updateSet { titles ->
            if (isChecked) titles.add(title) else titles.remove(title)
        }
    }

    override fun selectPlaylist(title: PlaylistTitle) {
        val srcPlaylist = requireNotNull(srcTitleToPlaylist.value?.get(title))
        val dstPlaylist = dstTitleToPlaylist.value?.get(title)

        playlistCache.setPlaylist(fromService, srcPlaylist)

        if (dstPlaylist != null) {
            playlistCache.setPlaylist(toService, dstPlaylist)
        }

        navigateTo(
            UserContentFragmentDirections.actionUserContentToPlaylist(
                fromService.id, toService.id, srcPlaylist.title
            )
        )
    }

    override fun migrate() {
        viewModelScope.launch {
            launch(Dispatchers.IO) {
                migrateTracks()
            }

            launch(Dispatchers.IO) {
                migratePlaylists()
            }
        }
    }

    override fun startAudioPreview(id: ID) {
        val track = srcIdToTrack.value?.get(id)
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
        val ids = srcIdToTrack.value?.filter { (_, track) ->
            track.entity.audioPreviewUrl != null
        }?.keys?.toList() ?: return
        val nextId = ids.getOrNull(ids.indexOf(currentId) + 1)
            ?: ids.getOrNull(0)
            ?: return

        startAudioPreview(nextId)
    }

    override fun notifyOnPlayerDismissed() {
        playerState.value = null
    }

    private suspend fun migrateTracks() {
        val trackIds = if (isInSelectionMode.value) {
            tracksSelectedForMigration.value
        } else {
            srcIdToDstId.value?.keys ?: emptySet()
        }

        withContext(Dispatchers.Default) {
            launch {
                migrationUtils
                    .migrateTracksToLibrary(
                        addTracksToLibraryUC,
                        trackIds,
                        srcIdToDstId.value ?: emptyMap()
                    )
                    .map { newState ->
                        tracksMigrationState.value.toMutableMap().also { tracksMigrationState ->
                            tracksMigrationState.putAll(newState)
                        }
                    }
                    .collectTo(tracksMigrationState)
            }
        }
    }

    private suspend fun migratePlaylists() {
        val srcTitleToPlaylist = srcTitleToPlaylist.value ?: emptyMap()
        val migratedPlaylistIds =
            playlistsMigrationState
                .value
                .filterValues { migrationState ->
                    migrationState is PlaylistMigrationState.Migrated ||
                            migrationState is PlaylistMigrationState.PartiallyMigrated
                }
                .keys

        val playlistTitlesToMigrate = if (isInSelectionMode.value) {
            playlistsSelectedForMigration.value
        } else {
            srcTitleToPlaylist.keys
        }.filter { playlistTitle ->
            srcTitleToPlaylist[playlistTitle]?.id !in migratedPlaylistIds
        }

        playlistTitlesToMigrate.forEach { playlistTitle ->
            val srcPlaylistId = srcTitleToPlaylist[playlistTitle]?.id

            if (srcPlaylistId != null) {
                migrationUtils.migrateTracksToPlaylist(
                    createPlaylistUC = createPlaylistUC,
                    addTracksToPlaylistUC = addTracksToPlaylistUC,
                    playlistTitle = playlistTitle,
                    dstPlaylistId = dstTitleToPlaylist.value?.get(playlistTitle)?.id,
                    srcTrackIds = srcTitleToPlaylist[playlistTitle]
                        ?.tracks
                        ?.mapNotNull { it.ids[fromService.id] }
                        ?.toSet() ?: emptySet(),
                    srcToDstTrackId = srcIdToDstIdWithinPlaylist.value ?: emptyMap()
                ).collect { state ->
                    this.srcTitleToPlaylist.updateNullableMap { srcTitleToPlaylist ->
                        if (srcTitleToPlaylist != null) {
                            val oldPlaylist = checkNotNull(srcTitleToPlaylist[playlistTitle])
                            val newPlaylist = oldPlaylist.copy(
                                tracks = oldPlaylist.tracks.map { track ->
                                    val dstTrackId = state.tracksMigrationState.keys.find {
                                        it.srcTrackId == track.ids[fromService.id]
                                    }?.dstTrackId

                                    if (dstTrackId == null) {
                                        track
                                    }
                                    else {
                                        track.copy(
                                            ids = track.ids.toMutableMap().also {
                                                it[toService.id] = dstTrackId
                                            }
                                        )
                                    }
                                }
                            )

                            srcTitleToPlaylist[playlistTitle] = newPlaylist
                        }
                    }
                    this.playlistsMigrationState.updateMap { playlistsMigrationState ->
                        playlistsMigrationState[srcPlaylistId] = state.playlistMigrationState
                    }
                }
            }
        }
    }

    override fun handlePlaylistToUserContentResult(
        result: PlaylistToUserContentResult
    ) {
        val srcPlaylist = result.srcPlaylist
        val dstPlaylist = result.dstPlaylist
        val dstPlaylistMigrationState = result.dstPlaylistMigrationState

        srcTitleToPlaylist.updateNullableMap { srcTitleToPlaylist ->
            if (srcTitleToPlaylist == null) {
                mapOf(srcPlaylist.title to srcPlaylist)
            } else {
                srcTitleToPlaylist[srcPlaylist.title] = srcPlaylist
            }
        }

        if (dstPlaylist != null) {
            dstTitleToPlaylist.updateNullableMap { dstTitleToPlaylist ->
                if (dstTitleToPlaylist == null) {
                    mapOf(dstPlaylist.title to dstPlaylist)
                } else {
                    dstTitleToPlaylist[dstPlaylist.title] = dstPlaylist
                }
            }
        }

        if (dstPlaylistMigrationState != null) {
            playlistsMigrationState.updateMap { playlistsMigrationState ->
                playlistsMigrationState[srcPlaylist.id] = dstPlaylistMigrationState
            }
        }
    }

}
