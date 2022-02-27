package me.squeezymo.usersupport.api.domain.usecase

import kotlinx.coroutines.flow.Flow
import me.squeezymo.usersupport.api.data.model.ChatMessage

interface IReceiveMessagesFromChatUC {

    fun receiveMessages(): Flow<List<ChatMessage>>

}
