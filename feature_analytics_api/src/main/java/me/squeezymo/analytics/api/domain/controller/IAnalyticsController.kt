package me.squeezymo.analytics.api.domain.controller

import me.squeezymo.analytics.api.data.IUserFlowEvent

interface IAnalyticsController {

    fun appendUserFlowEvent(event: IUserFlowEvent)

    fun getUserFlowEvents(): List<IUserFlowEvent>

}
