package me.squeezymo.usersupport.impl.domain.usecase

import me.squeezymo.usersupport.api.data.repository.IChatRepository
import me.squeezymo.usersupport.api.domain.usecase.ISendMessageToChatUC
import javax.inject.Inject

internal class SendMessageToChatUC @Inject constructor(
    private val chatRepository: IChatRepository
) : ISendMessageToChatUC {

    override suspend fun sendMessage(message: String) {
        chatRepository.addMessage(message)
    }

}
