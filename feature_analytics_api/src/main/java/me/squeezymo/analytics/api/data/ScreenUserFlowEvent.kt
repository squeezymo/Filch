package me.squeezymo.analytics.api.data

data class ScreenUserFlowEvent(
    private val screenName: String
) : IUserFlowEvent {

    override fun getMessage() = screenName

    override fun getType() = "screen"

}