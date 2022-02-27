package me.squeezymo.usersupport.impl.domain.usecase

import kotlinx.coroutines.flow.first
import me.squeezymo.usersupport.api.data.repository.IChatRepository
import me.squeezymo.usersupport.api.domain.usecase.IMarkAllChatMessagesAsReadChatUC
import javax.inject.Inject

internal class MarkAllChatMessagesAsReadUC @Inject constructor(
    private val chatRepository: IChatRepository
) : IMarkAllChatMessagesAsReadChatUC {

    override suspend fun markAllMessagesAsRead() {
        chatRepository
            .markMessagesAsRead(
                chatRepository
                    .getMessages()
                    .first()
                    .filter { message ->
                        !message.isFromUser
                    }
                    .map { message ->
                        message.id
                    }
            )
    }

}
