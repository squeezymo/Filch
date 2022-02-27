package me.squeezymo.analytics.api.data

interface IUserFlowEvent {

    fun getType(): String

    fun getMessage(): String

}
