package me.squeezymo.usercontent.impl.ui.uistate

sealed class SelectionModeUiState {

    object None : SelectionModeUiState()

    object Off : SelectionModeUiState()

    object On : SelectionModeUiState()

}
