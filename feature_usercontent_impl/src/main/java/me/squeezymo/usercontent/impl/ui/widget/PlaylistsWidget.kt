package me.squeezymo.usercontent.impl.ui.widget

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.RecyclerView
import com.hannesdorfmann.adapterdelegates4.AsyncListDifferDelegationAdapter
import me.squeezymo.core.domain.data.PlaylistTitle
import me.squeezymo.core.ext.dp
import me.squeezymo.core.ui.recyclerview.BaseAdapterDelegate
import me.squeezymo.core.ui.recyclerview.BaseDiffItemCallback
import me.squeezymo.core.ui.recyclerview.IListItem
import me.squeezymo.core.ui.recyclerview.SimpleSpacingItemDecoration
import me.squeezymo.usercontent.impl.databinding.VPlaylistsBinding
import me.squeezymo.usercontent.impl.ui.viewobject.UserContentItemUi

internal class PlaylistsWidget @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    private val binding = VPlaylistsBinding.inflate(
        LayoutInflater.from(context),
        this
    )

    private lateinit var contentAdapter: AsyncListDifferDelegationAdapter<IListItem>

    init {
        binding.playlistsRcv.setHasFixedSize(true)
        binding.playlistsRcv.addItemDecoration(
            SimpleSpacingItemDecoration(
                orientation = RecyclerView.HORIZONTAL,
                spacePx = dp(12)
            )
        )
    }

    fun setState(state: UserContentItemUi.Playlists) {
        contentAdapter.items = state.items
    }

    private fun setOnPlaylistPickedListener(
        onPlaylistPicked: (PlaylistTitle) -> Unit,
        onCheckedForMigrationChanged: (id: PlaylistTitle, isChecked: Boolean) -> Unit
    ) {
        contentAdapter = AsyncListDifferDelegationAdapter(
            BaseDiffItemCallback(),
            PlaylistLoadingWidget.createAdapterDelegate(),
            PlaylistLoadedWidget.createAdapterDelegate(
                onPlaylistPicked,
                onCheckedForMigrationChanged
            )
        )
        binding.playlistsRcv.adapter = contentAdapter
        (binding.playlistsRcv.itemAnimator as? DefaultItemAnimator)?.supportsChangeAnimations = false
    }

    companion object {

        private fun create(
            context: Context,
            onPlaylistPicked: (PlaylistTitle) -> Unit,
            onCheckedForMigrationChanged: (id: PlaylistTitle, isChecked: Boolean) -> Unit
        ): PlaylistsWidget {
            return PlaylistsWidget(context).also {
                it.setOnPlaylistPickedListener(onPlaylistPicked, onCheckedForMigrationChanged)
            }
        }

        fun createAdapterDelegate(
            onPlaylistPicked: (PlaylistTitle) -> Unit,
            onCheckedForMigrationChanged: (id: PlaylistTitle, isChecked: Boolean) -> Unit
        ) = BaseAdapterDelegate(
            UserContentItemUi.Playlists::class,
            { context ->
                create(context, onPlaylistPicked, onCheckedForMigrationChanged)
            },
            updateState = { widget, state, _ ->
                widget.setState(state)
            }
        )

    }

}
