package me.squeezymo.analytics.api.ui.delegate

import androidx.lifecycle.LifecycleOwner
import me.squeezymo.analytics.api.domain.controller.IAnalyticsController

interface IAnalyticsViewDelegate : IAnalyticsController {

    fun bind(
        lifecycleOwner: LifecycleOwner,
        screenName: () -> String
    )

    fun appendEvent(message: String)

    fun appendScreenEvent(screenName: String)

}
