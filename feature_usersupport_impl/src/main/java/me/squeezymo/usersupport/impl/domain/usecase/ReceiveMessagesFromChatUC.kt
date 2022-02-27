package me.squeezymo.usersupport.impl.domain.usecase

import kotlinx.coroutines.flow.Flow
import me.squeezymo.usersupport.api.data.model.ChatMessage
import me.squeezymo.usersupport.api.data.repository.IChatRepository
import me.squeezymo.usersupport.api.domain.usecase.IReceiveMessagesFromChatUC
import javax.inject.Inject

internal class ReceiveMessagesFromChatUC @Inject constructor(
    private val chatRepository: IChatRepository
) : IReceiveMessagesFromChatUC {

    override fun receiveMessages(): Flow<List<ChatMessage>> {
        return chatRepository.getMessages()
    }

}
