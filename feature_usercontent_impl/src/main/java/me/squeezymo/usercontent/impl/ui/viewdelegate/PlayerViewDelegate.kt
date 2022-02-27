package me.squeezymo.usercontent.impl.ui.viewdelegate

import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.media.MediaPlayer
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import androidx.core.animation.doOnEnd
import androidx.core.animation.doOnStart
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.*
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.launch
import me.squeezymo.core.domain.data.ID
import me.squeezymo.core.ui.gesture.SwipeDismissTouchListener
import me.squeezymo.usercontent.impl.ui.data.PlaybackPosition
import me.squeezymo.usercontent.impl.ui.data.PlaybackState
import me.squeezymo.usercontent.impl.ui.uistate.PlayerUiState
import me.squeezymo.usercontent.impl.ui.vmdelegate.IPlayerVmDelegate
import me.squeezymo.usercontent.impl.ui.widget.PlayerWidget
import java.util.*
import kotlin.collections.LinkedHashSet

internal fun Fragment.addPlayerViewDelegate(
    vmDelegate: IPlayerVmDelegate,
    playerWidget: PlayerWidget
): IPlayerViewDelegate {
    return PlayerViewDelegate(vmDelegate, playerWidget).also {
        it.startObserving(this)
    }
}

internal interface IPlayerViewDelegate {

    fun getTrackId(): ID?

    fun getPlaybackState(): PlaybackState?

    fun getPlaybackPosition(): PlaybackPosition?

    fun togglePlayback(id: ID)

    fun requestPosition(position: Int)

    fun addPlaybackListener(listener: Listener)

    fun removePlaybackListener(listener: Listener)

    interface Listener {

        fun onTrackIdChanged(
            trackId: ID?
        )

        fun onPlaybackStateChanged(
            state: PlaybackState?
        )

        fun onPlaybackPositionChanged(
            playbackPosition: PlaybackPosition?
        )

    }

}

@SuppressLint("ClickableViewAccessibility")
internal class PlayerViewDelegate(
    private val vmDelegate: IPlayerVmDelegate,
    private val playerWidget: PlayerWidget
) : IPlayerViewDelegate {

    private lateinit var player: MediaPlayer
    private var isPlayerReleased = true
    private val listeners = LinkedHashSet<IPlayerViewDelegate.Listener>()

    private var lastKnownState: PlayerUiState? = null
    private var lastKnownPlaybackState: PlaybackState? = null
    private var lastKnownPlaybackPosition: PlaybackPosition? = null

    private var autoplayWhenPrepared = true
    private var timer: Timer? = null

    init {
        playerWidget.setOnPauseListener(::pause)
        playerWidget.setOnResumeListener(::resume)
        playerWidget.setOnNextListener(::next)
        playerWidget.setCheckedForMigrationChangedListener(vmDelegate::selectTrackForMigration)
        playerWidget.setOnPositionRequestedListener(::requestPosition)
        playerWidget.setOnTouchListener(
            SwipeDismissTouchListener(
                playerWidget,
                null,
                object : SwipeDismissTouchListener.DismissCallbacks {
                    override fun onDismiss(view: View?, token: Any?) {
                        if (player.isPlaying) {
                            pause()
                        }

                        playerWidget.isGone = true
                        vmDelegate.notifyOnPlayerDismissed()
                    }

                    override fun canDismiss(token: Any?): Boolean {
                        return true
                    }
                }
            )
        )
    }

    private fun initPlayer(player: MediaPlayer) {
        player.isLooping = false
        player.setOnPreparedListener {
            if (autoplayWhenPrepared) {
                resume()
            } else {
                notifyOnPlaybackState(PlaybackState.Prepared)
                notifyOnPlaybackPosition(getCurrentPlaybackPosition())
            }
        }
        player.setOnCompletionListener {
            player.seekTo(0)
            notifyOnPlaybackState(PlaybackState.Prepared)
        }
        player.setOnSeekCompleteListener {
            notifyOnPlaybackPosition(getCurrentPlaybackPosition(isNewSeekPosition = true))
        }
    }

    fun startObserving(fragment: Fragment) {
        fragment.viewLifecycleOwner.lifecycleScope.launch {
            fragment.repeatOnLifecycle(Lifecycle.State.STARTED) {
                updatePlayer(vmDelegate.playerUiState.value, true)

                launch {
                    vmDelegate
                        .playerUiState
                        .drop(1)
                        .collect { state ->
                            updatePlayer(state, false)
                        }
                }
            }
        }

        fragment.viewLifecycleOwner.lifecycle.addObserver(
            object : DefaultLifecycleObserver {
                override fun onCreate(owner: LifecycleOwner) {
                    player = MediaPlayer()
                    initPlayer(player)
                    isPlayerReleased = false
                }

                override fun onStop(owner: LifecycleOwner) {
                    if (player.isPlaying) {
                        pause()
                    }
                }

                override fun onDestroy(owner: LifecycleOwner) {
                    player.release()
                    isPlayerReleased = true
                    timer?.cancel()
                    timer?.purge()
                }
            }
        )
    }

    override fun getTrackId(): ID? {
        return (lastKnownState as? PlayerUiState.Player)?.trackId
    }

    override fun getPlaybackState(): PlaybackState? {
        return lastKnownPlaybackState
    }

    override fun getPlaybackPosition(): PlaybackPosition? {
        return lastKnownPlaybackPosition
    }

    override fun togglePlayback(id: ID) {
        if ((lastKnownState as? PlayerUiState.Player)?.trackId == id) {
            if (player.isPlaying) {
                pause()
            }
            else {
                resume()
            }
        }
        else {
            vmDelegate.startAudioPreview(id)
        }
    }

    override fun requestPosition(position: Int) {
        player.seekTo(position)
    }

    override fun addPlaybackListener(
        listener: IPlayerViewDelegate.Listener
    ) {
        listeners.add(listener)
    }

    override fun removePlaybackListener(
        listener: IPlayerViewDelegate.Listener
    ) {
        listeners.remove(listener)
    }

    private fun updatePlayer(uiState: PlayerUiState, isDelayed: Boolean) {
        val currentState = lastKnownState
        val isSameUrl = uiState is PlayerUiState.Player
                && currentState is PlayerUiState.Player
                && uiState.url == currentState.url

        if (!isSameUrl) {
            player.reset()

            when (uiState) {
                is PlayerUiState.None -> {
                    hidePlayer()
                }
                is PlayerUiState.Player -> {
                    playerWidget.setState(uiState)
                    notifyOnPlaybackState(PlaybackState.Preparing)
                    showPlayer(uiState.trackId)

                    autoplayWhenPrepared = !isDelayed

                    player.setDataSource(uiState.url)
                    player.prepareAsync()
                }
            }
        }

        if (uiState is PlayerUiState.Player) {
            playerWidget.notifyOnMigrationState(
                dstService = uiState.dstService,
                isInSelectionMode = uiState.isInSelectionMode,
                migrationStatus = uiState.migrationStatusUi
            )
        }

        lastKnownState = uiState
        listeners.forEach { listener ->
            listener.onTrackIdChanged((lastKnownState as? PlayerUiState.Player)?.trackId)
        }
    }

    private fun showPlayer(trackId: ID) {
        if (playerWidget.isVisible) {
            return
        }

        listeners.forEach { listener ->
            listener.onTrackIdChanged(trackId)
        }

        ValueAnimator
            .ofFloat(0F, 1F)
            .apply {
                addUpdateListener { valueAnimator ->
                    val value = valueAnimator.animatedValue as Float

                    playerWidget.alpha = value
                    playerWidget.translationY = (value - 1) * playerWidget.height
                }
                doOnStart {
                    playerWidget.alpha = 0F
                    playerWidget.isVisible = true
                }
                interpolator = AccelerateDecelerateInterpolator()
                duration = 200L
            }
            .start()
    }

    private fun hidePlayer() {
        if (playerWidget.isGone) {
            return
        }

        listeners.forEach { listener ->
            listener.onTrackIdChanged(null)
        }

        ValueAnimator
            .ofFloat(1F, 0F)
            .apply {
                addUpdateListener { valueAnimator ->
                    val value = valueAnimator.animatedValue as Float

                    playerWidget.alpha = value
                    playerWidget.translationY = (value - 1) * playerWidget.height
                }
                doOnEnd {
                    playerWidget.isGone = true
                }
                interpolator = AccelerateDecelerateInterpolator()
                duration = 200L
            }
            .start()
    }

    private fun pause() {
        timer?.cancel()

        player.pause()
        notifyOnPlaybackState(PlaybackState.Paused)
    }

    private fun resume() {
        player.start()
        notifyOnPlaybackState(PlaybackState.Resumed)

        timer = Timer().apply {
            scheduleAtFixedRate(
                object : TimerTask() {
                    override fun run() {
                        notifyOnPlaybackPosition(getCurrentPlaybackPosition())
                    }
                },
                0L,
                75L
            )
        }
    }

    private fun next() {
        vmDelegate.startNextAudioPreview()
    }

    private fun notifyOnPlaybackState(
        state: PlaybackState
    ) {
        lastKnownPlaybackState = state
        listeners.forEach { listener ->
            listener.onPlaybackStateChanged(state)
        }

        playerWidget.notifyOnPlaybackState(state)
    }

    private fun notifyOnPlaybackPosition(
        playbackPosition: PlaybackPosition,
    ) {
        lastKnownPlaybackPosition = playbackPosition
        listeners.forEach { listener ->
            listener.onPlaybackPositionChanged(playbackPosition)
        }

        playerWidget.notifyOnPlaybackPosition(playbackPosition)
    }

    private fun getCurrentPlaybackPosition(
        isNewSeekPosition: Boolean = false
    ): PlaybackPosition {
        if (isPlayerReleased) {
            return PlaybackPosition.Unknown
        }

        try {
            val duration = player.duration

            if (duration == -1) {
                return PlaybackPosition.Unknown
            }

            return PlaybackPosition.Known(
                player.currentPosition,
                duration,
                isNewSeekPosition
            )
        }
        catch (e: Exception) {
            return PlaybackPosition.Unknown
        }
    }

}
