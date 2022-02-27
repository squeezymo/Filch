package me.squeezymo.usercontent.impl.ui.widget

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.CompoundButton
import android.widget.LinearLayout
import android.widget.SeekBar
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import androidx.transition.ChangeBounds
import androidx.transition.Fade
import androidx.transition.TransitionManager
import androidx.transition.TransitionSet
import me.squeezymo.core.domain.data.ID
import me.squeezymo.core.ext.getColorCompat
import me.squeezymo.core.ext.setTextOrMakeGone
import me.squeezymo.core.ext.setTextOrMakeInvisible
import me.squeezymo.core.ext.visibleOrGone
import me.squeezymo.core.ui.recyclerview.BaseAdapterDelegate
import me.squeezymo.usercontent.impl.R
import me.squeezymo.usercontent.impl.databinding.VTrackBinding
import me.squeezymo.usercontent.impl.ui.data.PlaybackPosition
import me.squeezymo.usercontent.impl.ui.data.PlaybackState
import me.squeezymo.usercontent.impl.ui.utils.MigrationStatusUtils
import me.squeezymo.usercontent.impl.ui.viewdelegate.IPlayerViewDelegate
import me.squeezymo.usercontent.impl.ui.viewobject.MigrationStatusUi
import me.squeezymo.usercontent.impl.ui.viewobject.UserContentItemUi

internal class TrackWidget @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    private val binding = VTrackBinding.inflate(
        LayoutInflater.from(context),
        this
    )

    private var trackId: ID? = null

    private lateinit var onCheckedForMigrationChanged: (id: ID, isChecked: Boolean) -> Unit
    private var playerDelegate: IPlayerViewDelegate? = null
    private var seekBarCanAdvance = true

    private val onSelectedForMigrationCheckedStateChanged =
        CompoundButton.OnCheckedChangeListener { _, isChecked ->
            onCheckedForMigrationChanged(requireNotNull(trackId), isChecked)
        }
    private val playbackListener = PlaybackListener()

    init {
        orientation = VERTICAL

        binding.migrateCb.setOnCheckedChangeListener(onSelectedForMigrationCheckedStateChanged)

        // Only works when set programmatically. `android:padding="0dp"` has no effect
        binding.trackSb.setPadding(0, 0, 0, 0)
        binding.trackSb.setOnSeekBarChangeListener(
            object : SeekBar.OnSeekBarChangeListener {
                private var lastProgressFromUser: Int = 0

                override fun onStartTrackingTouch(seekBar: SeekBar?) {
                    seekBarCanAdvance = false
                }

                override fun onStopTrackingTouch(seekBar: SeekBar?) {
                    playerDelegate?.requestPosition(lastProgressFromUser)
                }

                override fun onProgressChanged(
                    seekBar: SeekBar?,
                    progress: Int,
                    fromUser: Boolean
                ) {
                    if (fromUser) {
                        lastProgressFromUser = progress
                    }
                }
            }
        )
    }

    fun setState(state: UserContentItemUi.Track) {
        this.trackId = state.id

        binding.thumbnailWidget.setState(state.thumbnail)

        if (state.thumbnail.hasAudioPreview) {
            binding.thumbnailWidget.setOnClickListener {
                playerDelegate?.togglePlayback(state.id)
            }
        } else {
            binding.thumbnailWidget.setOnClickListener(null)
        }

        binding.titleTv.text = state.title
        binding.artistTv.setTextOrMakeGone(state.artist)
        binding.durationTv.setTextOrMakeInvisible(state.duration)

        MigrationStatusUtils.updateMigrationStatus(
            dstService = state.dstService,
            migrationStatus = state.migrationStatus,
            isInSelectionMode = state.isInSelectionMode,
            migrationCompleteIv = binding.migrationCompleteIv,
            migrateCb = binding.migrateCb,
            migrationStatusRetrievingPi = binding.migrationStatusRetrievingPi,
            migrationProgressPi = binding.migrationProgressPi,
            onSelectedForMigrationCheckedStateChanged = onSelectedForMigrationCheckedStateChanged
        )

        setBackgroundColor(
            if (state.isInSelectionMode
                && state.migrationStatus is MigrationStatusUi.NotMigrated
                && state.migrationStatus.isSelected
            ) {
                context.getColorCompat(R.color.white_a12)
            } else {
                Color.TRANSPARENT
            }
        )

        binding.emptyPlaceholder.visibleOrGone {
            !binding.migrationCompleteIv.isVisible &&
                    !binding.migrateCb.isVisible &&
                    !binding.migrationProgressPi.isVisible
        }

        binding.thumbnailWidget.setDimmed(
            binding.migrationStatusRetrievingPi.isVisible
        )

        resetPlayerDelegate()
    }

    fun setCheckedForMigrationChangedListener(
        onCheckedForMigrationChanged: (id: ID, isChecked: Boolean) -> Unit
    ) {
        this.onCheckedForMigrationChanged = onCheckedForMigrationChanged
    }

    fun setPlayerViewDelegate(delegate: IPlayerViewDelegate) {
        this.playerDelegate = delegate

        resetPlayerDelegate()

        if (isAttachedToWindow) {
            attachPlaybackListener()
        }
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        attachPlaybackListener()
    }

    override fun onDetachedFromWindow() {
        removePlaybackListener()
        super.onDetachedFromWindow()
    }

    private fun resetPlayerDelegate() {
        playerDelegate?.let { delegate ->
            playbackListener.onTrackIdChanged(delegate.getTrackId())
            playbackListener.onPlaybackStateChanged(delegate.getPlaybackState())
            playbackListener.onPlaybackPositionChanged(delegate.getPlaybackPosition())
        }
    }

    private fun attachPlaybackListener() {
        playerDelegate?.addPlaybackListener(playbackListener)
    }

    private fun removePlaybackListener() {
        playerDelegate?.removePlaybackListener(playbackListener)
    }

    private inner class PlaybackListener : IPlayerViewDelegate.Listener {
        private var shouldObserveChanges: Boolean = false

        override fun onTrackIdChanged(trackId: ID?) {
            val newShouldObserveChanges =
                this@TrackWidget.trackId != null && this@TrackWidget.trackId == trackId

            if (shouldObserveChanges == newShouldObserveChanges) {
                return
            }

            shouldObserveChanges = newShouldObserveChanges

            if (!shouldObserveChanges) {
                binding.thumbnailWidget.setPlaybackState(null)
            }

            TransitionManager.beginDelayedTransition(
                binding.trackSb.parent as ViewGroup,
                TransitionSet().also { transitionSet ->
                    transitionSet.ordering = TransitionSet.ORDERING_SEQUENTIAL

                    transitionSet.addTransition(ChangeBounds())
                    transitionSet.addTransition(
                        Fade().also { fade ->
                            fade.addTarget(binding.trackSb)
                        }
                    )
                }
            )

            binding.trackSb.visibleOrGone {
                shouldObserveChanges
            }
        }

        override fun onPlaybackStateChanged(state: PlaybackState?) {
            if (shouldObserveChanges) {
                binding.thumbnailWidget.setPlaybackState(state)
            }
        }

        override fun onPlaybackPositionChanged(playbackPosition: PlaybackPosition?) {
            if (shouldObserveChanges) {
                when (playbackPosition) {
                    null, PlaybackPosition.Unknown -> {
                        binding.trackSb.isInvisible = true
                    }
                    is PlaybackPosition.Known -> {
                        binding.trackSb.isVisible = true

                        if (playbackPosition.isNewSeekPosition) {
                            seekBarCanAdvance = true
                        }

                        if (seekBarCanAdvance) {
                            binding.trackSb.max = playbackPosition.durationMillis
                            binding.trackSb.progress = playbackPosition.positionMillis
                        }
                    }
                }
            }
        }

    }

    companion object {

        fun createAdapterDelegate(
            onCheckedForMigrationChanged: (id: ID, isChecked: Boolean) -> Unit,
            playerDelegate: IPlayerViewDelegate
        ) = BaseAdapterDelegate(
            UserContentItemUi.Track::class,
            createWidget = { context ->
                create(
                    context,
                    onCheckedForMigrationChanged,
                    playerDelegate
                )
            },
            updateState = { widget, state, _ ->
                widget.setState(state)
            }
        )

        private fun create(
            context: Context,
            onCheckedForMigrationChanged: (id: ID, isChecked: Boolean) -> Unit,
            playerDelegate: IPlayerViewDelegate
        ): TrackWidget {
            return TrackWidget(context).apply {
                setCheckedForMigrationChangedListener(onCheckedForMigrationChanged)
                setPlayerViewDelegate(playerDelegate)
            }
        }

    }

}
