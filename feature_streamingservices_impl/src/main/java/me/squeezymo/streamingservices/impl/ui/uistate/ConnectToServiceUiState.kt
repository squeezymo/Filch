package me.squeezymo.streamingservices.impl.ui.uistate

import me.squeezymo.streamingservices.api.domain.data.StreamingService

internal data class ConnectToServiceUiState(
    val service: StreamingService,
    val isConnected: Boolean
)
