package me.squeezymo.usercontent.impl.ui.uistate

sealed class ShowMigratedTracksUiState {

    object None : ShowMigratedTracksUiState()

    object Shown : ShowMigratedTracksUiState()

    object Hidden : ShowMigratedTracksUiState()

}
