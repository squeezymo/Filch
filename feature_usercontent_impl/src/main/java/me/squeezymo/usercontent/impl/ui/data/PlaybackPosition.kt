package me.squeezymo.usercontent.impl.ui.data

internal sealed class PlaybackPosition {

    object Unknown : PlaybackPosition()

    data class Known(
        val positionMillis: Int,
        val durationMillis: Int,
        val isNewSeekPosition: Boolean
    ) : PlaybackPosition()

}
