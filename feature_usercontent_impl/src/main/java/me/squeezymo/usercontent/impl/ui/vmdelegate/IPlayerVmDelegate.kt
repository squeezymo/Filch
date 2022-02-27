package me.squeezymo.usercontent.impl.ui.vmdelegate

import kotlinx.coroutines.flow.StateFlow
import me.squeezymo.core.domain.data.ID
import me.squeezymo.usercontent.impl.ui.uistate.PlayerUiState

interface IPlayerVmDelegate {

    val playerUiState: StateFlow<PlayerUiState>

    fun startAudioPreview(id: ID)

    fun startNextAudioPreview()

    fun notifyOnPlayerDismissed()

    fun selectTrackForMigration(id: ID, isChecked: Boolean)

}
