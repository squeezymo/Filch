package me.squeezymo.usersupport.impl.data.repository.internal

import me.squeezymo.usersupport.api.data.model.ChatMessage
import me.squeezymo.usersupport.impl.network.dto.ChatMessageDTO
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
internal class ChatDtoToDataMapper @Inject constructor(

) {

    fun mapMessage(
        dto: ChatMessageDTO,
        userId: String,
        isFromCache: Boolean
    ): ChatMessage {
        return ChatMessage(
            id = dto.id,
            body = dto.body,
            isRead = dto.isRead,
            isFromUser = dto.senderId == userId,
            timestampMillis = TimeUnit.MILLISECONDS.convert(dto.date.seconds, TimeUnit.SECONDS),
            isFromCache = isFromCache
        )
    }

}
