package me.squeezymo.musicmigrator.ui.main.state

import me.squeezymo.streamingservices.api.domain.data.StreamingService

class StreamingServiceUiState(
    val streamingService: StreamingService,
    val isEnabled: Boolean,
    val isAuthenticated: Boolean
)
