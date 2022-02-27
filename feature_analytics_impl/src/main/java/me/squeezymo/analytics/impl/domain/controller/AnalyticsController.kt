package me.squeezymo.analytics.impl.domain.controller

import me.squeezymo.analytics.api.data.IUserFlowEvent
import me.squeezymo.analytics.api.domain.controller.IAnalyticsController
import java.util.*
import javax.inject.Inject

internal class AnalyticsController @Inject constructor(

): IAnalyticsController {

    private val flowAcc: MutableList<IUserFlowEvent> = LinkedList()

    override fun appendUserFlowEvent(event: IUserFlowEvent) {
        flowAcc.add(event)
    }

    override fun getUserFlowEvents(): List<IUserFlowEvent> {
        return flowAcc
    }

}
