package me.squeezymo.analytics.impl.ui.delegate

import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import me.squeezymo.analytics.api.data.GenericUserFlowEvent
import me.squeezymo.analytics.api.data.ScreenUserFlowEvent
import me.squeezymo.analytics.api.domain.controller.IAnalyticsController
import me.squeezymo.analytics.api.ui.delegate.IAnalyticsViewDelegate
import javax.inject.Inject

internal class AnalyticsViewDelegate @Inject constructor(
    private val controller: IAnalyticsController
) : IAnalyticsViewDelegate,
    IAnalyticsController by controller {

    override fun bind(lifecycleOwner: LifecycleOwner, screenName: () -> String) {
        lifecycleOwner.lifecycle.addObserver(
            AnalyticsLifecycleObserver(screenName)
        )
    }

    override fun appendEvent(message: String) {
        controller.appendUserFlowEvent(
            GenericUserFlowEvent(message)
        )
    }

    override fun appendScreenEvent(screenName: String) {
        controller.appendUserFlowEvent(
            ScreenUserFlowEvent(screenName)
        )
    }

    private inner class AnalyticsLifecycleObserver(
        private val screenName: () -> String
    ) : DefaultLifecycleObserver {

        override fun onStart(owner: LifecycleOwner) {
            appendScreenEvent(screenName())
        }

    }

}
