package me.squeezymo.usersupport.api.domain.usecase

interface ISendMessageToChatUC {

    suspend fun sendMessage(message: String)

}
