package me.squeezymo.usercontent.impl.ui

import androidx.lifecycle.SavedStateHandle
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import me.squeezymo.core.ext.toBundle
import me.squeezymo.core.ui.BaseViewModel
import me.squeezymo.core.ui.IBaseViewModel
import me.squeezymo.streamingservices.api.domain.data.StreamingService
import me.squeezymo.usercontent.impl.ui.event.PlaylistToUserContentResult
import javax.inject.Inject

internal interface IUserContentRootViewModel : IBaseViewModel {

    val fromService: StreamingService

    val toService: StreamingService

    val shouldAutoMigrate: Boolean

    val playlistToUserContentResult: Flow<PlaylistToUserContentResult?>

    fun sendOnPlaylistToUserContentResult(result: PlaylistToUserContentResult)

    fun notifyOnPlaylistToUserContentResultHandled()

}

@HiltViewModel
internal class UserContentRootViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle
) : BaseViewModel(savedStateHandle), IUserContentRootViewModel {

    private val args = UserContentRootFragmentArgs.fromBundle(savedStateHandle.toBundle())

    override val fromService = StreamingService.requireById(args.from)
    override val toService = StreamingService.requireById(args.to)
    override val shouldAutoMigrate = args.autoMigrate

    override val playlistToUserContentResult: MutableStateFlow<PlaylistToUserContentResult?> =
        MutableStateFlow(null)

    override fun sendOnPlaylistToUserContentResult(result: PlaylistToUserContentResult) {
        playlistToUserContentResult.value = result
    }

    override fun notifyOnPlaylistToUserContentResultHandled() {
        playlistToUserContentResult.value = null
    }

}
