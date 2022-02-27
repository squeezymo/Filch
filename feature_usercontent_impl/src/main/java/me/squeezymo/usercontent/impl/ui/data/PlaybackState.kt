package me.squeezymo.usercontent.impl.ui.data

internal sealed class PlaybackState {

    object Preparing : PlaybackState()

    object Prepared : PlaybackState()

    object Resumed : PlaybackState()

    object Paused : PlaybackState()

}