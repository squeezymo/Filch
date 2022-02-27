package me.squeezymo.usercontent.impl.ui.widget

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.CompoundButton
import android.widget.FrameLayout
import android.widget.SeekBar
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import androidx.transition.TransitionManager
import me.squeezymo.core.domain.data.ID
import me.squeezymo.core.ext.getColorCompat
import me.squeezymo.streamingservices.api.domain.data.StreamingService
import me.squeezymo.usercontent.impl.R
import me.squeezymo.usercontent.impl.databinding.VPlayerBinding
import me.squeezymo.usercontent.impl.ui.data.PlaybackPosition
import me.squeezymo.usercontent.impl.ui.data.PlaybackState
import me.squeezymo.usercontent.impl.ui.uistate.PlayerUiState
import me.squeezymo.usercontent.impl.ui.utils.MigrationStatusUtils
import me.squeezymo.usercontent.impl.ui.viewobject.MigrationStatusUi

internal class PlayerWidget @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    private val binding = VPlayerBinding.inflate(
        LayoutInflater.from(context),
        this
    )

    private lateinit var pause: () -> Unit
    private lateinit var resume: () -> Unit
    private lateinit var trackId: ID
    private lateinit var onCheckedForMigrationChanged: (id: ID, isChecked: Boolean) -> Unit
    private var seekBarCanAdvance = true

    private val onSelectedForMigrationCheckedStateChanged =
        CompoundButton.OnCheckedChangeListener { _, isChecked ->
            onCheckedForMigrationChanged(trackId, isChecked)
        }
    private lateinit var onPositionRequested: (position: Int) -> Unit

    init {
        setBackgroundResource(R.color.black_a80)

        // Only works when set programmatically. `android:padding="0dp"` has no effect
        binding.trackSb.setPadding(0, 0, 0, 0)
        binding.trackSb.setOnSeekBarChangeListener(
            object : SeekBar.OnSeekBarChangeListener {
                private var lastProgressFromUser: Int = 0

                override fun onStartTrackingTouch(seekBar: SeekBar?) {
                    seekBarCanAdvance = false
                }

                override fun onStopTrackingTouch(seekBar: SeekBar?) {
                    onPositionRequested(lastProgressFromUser)
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

    fun setState(state: PlayerUiState.Player) {
        trackId = state.trackId

        binding.trackTv.text = state.track
        binding.artistTv.text = state.artist
    }

    fun setOnPauseListener(onPause: () -> Unit) {
        pause = onPause
    }

    fun setOnResumeListener(onResume: () -> Unit) {
        resume = onResume
    }

    fun setOnNextListener(onNext: () -> Unit) {
        binding.nextIv.setOnClickListener {
            onNext()
        }
    }

    fun setCheckedForMigrationChangedListener(
        onCheckedForMigrationChanged: (id: ID, isChecked: Boolean) -> Unit
    ) {
        this.onCheckedForMigrationChanged = onCheckedForMigrationChanged
    }

    fun setOnPositionRequestedListener(
        onPositionRequested: (position: Int) -> Unit
    ) {
        this.onPositionRequested = onPositionRequested
    }

    fun notifyOnPlaybackState(state: PlaybackState) {
        when (state) {
            PlaybackState.Preparing -> {
                binding.togglePlayIv.setImageResource(R.drawable.ic_pause)
                enablePlayButton(null)
            }
            PlaybackState.Prepared -> {
                binding.togglePlayIv.setImageResource(R.drawable.ic_play)
                enablePlayButton(resume)
            }
            PlaybackState.Paused -> {
                binding.togglePlayIv.setImageResource(R.drawable.ic_play)
                enablePlayButton(resume)
            }
            PlaybackState.Resumed -> {
                binding.togglePlayIv.setImageResource(R.drawable.ic_pause)
                enablePlayButton(pause)
            }
        }
    }

    fun notifyOnPlaybackPosition(
        playbackPosition: PlaybackPosition
    ) {
        when (playbackPosition) {
            PlaybackPosition.Unknown -> {
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

    fun notifyOnMigrationState(
        dstService: StreamingService,
        isInSelectionMode: Boolean,
        migrationStatus: MigrationStatusUi
    ) {
        TransitionManager.beginDelayedTransition(this)

        MigrationStatusUtils.updateMigrationStatus(
            dstService = dstService,
            migrationStatus = migrationStatus,
            isInSelectionMode = isInSelectionMode,
            migrationCompleteIv = binding.migrationCompleteIv,
            migrateCb = binding.migrateCb,
            migrationStatusRetrievingPi = null,
            migrationProgressPi = binding.migrationProgressPi,
            onSelectedForMigrationCheckedStateChanged = onSelectedForMigrationCheckedStateChanged
        )
    }

    private fun enablePlayButton(
        listener: (() -> Unit)?
    ) {
        if (listener == null) {
            binding.togglePlayIv.setColorFilter(context.getColorCompat(R.color.white_a50))
            binding.togglePlayIv.setOnClickListener(null)
        } else {
            binding.togglePlayIv.setColorFilter(context.getColorCompat(R.color.white))
            binding.togglePlayIv.setOnClickListener {
                listener()
            }
        }
    }

}
