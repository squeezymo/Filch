package me.squeezymo.usercontent.impl.ui.widget

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.CompoundButton
import android.widget.LinearLayout
import com.bumptech.glide.Glide
import me.squeezymo.core.domain.data.PlaylistTitle
import me.squeezymo.core.ext.clipWithRoundRect
import me.squeezymo.core.ext.dp
import me.squeezymo.core.ext.getDrawableOrThrow
import me.squeezymo.core.ext.setTextOrMakeGone
import me.squeezymo.core.ui.recyclerview.BaseAdapterDelegate
import me.squeezymo.usercontent.impl.R
import me.squeezymo.usercontent.impl.databinding.VPlaylistLoadedBinding
import me.squeezymo.usercontent.impl.ui.utils.MigrationStatusUtils
import me.squeezymo.usercontent.impl.ui.viewobject.PlaylistUi

internal class PlaylistLoadedWidget @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    private val binding = VPlaylistLoadedBinding.inflate(
        LayoutInflater.from(context),
        this
    )

    private lateinit var state: PlaylistUi.Loaded
    private lateinit var onCheckedForMigrationChanged: (title: PlaylistTitle, isChecked: Boolean) -> Unit

    private val onSelectedForMigrationCheckedStateChanged =
        CompoundButton.OnCheckedChangeListener { _, isChecked ->
            onCheckedForMigrationChanged(state.title, isChecked)
        }

    init {
        orientation = VERTICAL
        showDividers = SHOW_DIVIDER_MIDDLE
        dividerDrawable = context.getDrawableOrThrow(R.drawable.space_h_8)

        binding.thumbnailIv.clipWithRoundRect(dp(4))
    }

    fun setState(state: PlaylistUi.Loaded) {
        binding.titleTv.setTextOrMakeGone(state.title)

        if (state.thumbnailUrl == null) {
            Glide
                .with(this)
                .clear(binding.thumbnailIv)
        } else {
            Glide
                .with(this)
                .load(state.thumbnailUrl)
                .into(binding.thumbnailIv)
        }

        MigrationStatusUtils.updateMigrationStatus(
            dstService = state.dstService,
            migrationStatus = state.migrationStatus,
            isInSelectionMode = state.isInSelectionMode,
            migrationCompleteIv = binding.migrationCompleteIv,
            migrateCb = binding.migrateCb,
            migrationStatusRetrievingPi = null,
            migrationProgressPi = binding.migrationProgressPi,
            onSelectedForMigrationCheckedStateChanged = onSelectedForMigrationCheckedStateChanged
        )

        this.state = state
    }

    fun setCheckedForMigrationChangedListener(
        onCheckedForMigrationChanged: (id: PlaylistTitle, isChecked: Boolean) -> Unit
    ) {
        this.onCheckedForMigrationChanged = onCheckedForMigrationChanged
    }

    private fun setOnPlaylistPickedListener(
        onPlaylistPicked: (PlaylistTitle) -> Unit
    ) {
        binding.root.setOnClickListener {
            onPlaylistPicked(state.title)
        }
    }

    companion object {

        private fun create(
            context: Context,
            onPlaylistPicked: (PlaylistTitle) -> Unit,
            onCheckedForMigrationChanged: (id: PlaylistTitle, isChecked: Boolean) -> Unit
        ): PlaylistLoadedWidget {
            return PlaylistLoadedWidget(context).also {
                it.setOnPlaylistPickedListener(onPlaylistPicked)
                it.setCheckedForMigrationChangedListener(onCheckedForMigrationChanged)
            }
        }

        fun createAdapterDelegate(
            onPlaylistPicked: (PlaylistTitle) -> Unit,
            onCheckedForMigrationChanged: (id: PlaylistTitle, isChecked: Boolean) -> Unit
        ) = BaseAdapterDelegate(
            PlaylistUi.Loaded::class,
            { context ->
                create(context, onPlaylistPicked, onCheckedForMigrationChanged)
            },
            widgetWidth = ViewGroup.LayoutParams.WRAP_CONTENT,
            widgetHeight = ViewGroup.LayoutParams.WRAP_CONTENT,
            updateState = { widget, state, _ ->
                widget.setState(state)
            }
        )

    }

}
