package me.squeezymo.usersupport.api.data.repository

import kotlinx.coroutines.flow.Flow
import me.squeezymo.usersupport.api.data.model.ChatMessage

interface IChatRepository {

    fun getMessages(): Flow<List<ChatMessage>>

    suspend fun markMessagesAsRead(messageIds: List<String>)

    suspend fun addMessage(message: String)

}
