package me.squeezymo.usersupport.impl.data.repository

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import me.squeezymo.analytics.api.domain.controller.IAnalyticsController
import me.squeezymo.usersupport.api.data.model.ChatMessage
import me.squeezymo.usersupport.api.data.repository.IChatRepository
import me.squeezymo.usersupport.impl.data.repository.internal.ChatDtoToDataMapper
import me.squeezymo.usersupport.impl.data.repository.internal.ChatUserIdHolder
import me.squeezymo.usersupport.impl.network.IChatDataSource
import javax.inject.Inject

internal class ChatRepository @Inject constructor(
    @ApplicationContext context: Context,
    private val dataSource: IChatDataSource,
    private val mapper: ChatDtoToDataMapper,
    private val analyticsController: IAnalyticsController
) : IChatRepository {

    private val userId = ChatUserIdHolder(context).getUserId()

    override fun getMessages(): Flow<List<ChatMessage>> {
        return dataSource
            .getMessagesFlow(userId)
            .map { dtos ->
                dtos
                    .map { cacheable ->
                        mapper.mapMessage(cacheable.data, userId, cacheable.isFromCache)
                    }
                    .sortedBy(ChatMessage::timestampMillis)
            }
    }

    override suspend fun markMessagesAsRead(
        messageIds: List<String>
    ) {
        dataSource.markMessagesAsRead(userId, messageIds)
    }

    override suspend fun addMessage(message: String) {
        dataSource.addMessage(userId, message, analyticsController.getUserFlowEvents())
    }

}
