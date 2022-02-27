package me.squeezymo.analytics.api.data

data class GenericUserFlowEvent(
    private val message: String
) : IUserFlowEvent {

    override fun getMessage() = message

    override fun getType() = "event"

}