package me.squeezymo.settings.impl.ui.event

import me.squeezymo.streamingservices.api.domain.data.StreamingService

internal data class LogoutResultEvent(
    val streamingService: StreamingService,
    val isSuccess: Boolean
)
