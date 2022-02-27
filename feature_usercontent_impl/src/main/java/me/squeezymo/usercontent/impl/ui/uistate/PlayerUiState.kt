package me.squeezymo.usercontent.impl.ui.uistate

import me.squeezymo.core.domain.data.ID
import me.squeezymo.streamingservices.api.domain.data.StreamingService
import me.squeezymo.usercontent.impl.ui.viewobject.MigrationStatusUi

sealed class PlayerUiState {

    object None : PlayerUiState()

    data class Player(
        val trackId: ID,
        val url: String,
        val track: String,
        val artist: String?,
        val isInSelectionMode: Boolean,
        val dstService: StreamingService,
        val migrationStatusUi: MigrationStatusUi
    ) : PlayerUiState()

}
