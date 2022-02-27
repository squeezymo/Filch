package me.squeezymo.usersupport.impl.ui.mapper

import dagger.hilt.android.scopes.ViewModelScoped
import me.squeezymo.usersupport.api.data.model.ChatMessage
import me.squeezymo.usersupport.impl.ui.viewobject.ChatMessageUi
import javax.inject.Inject

@ViewModelScoped
internal class ChatDataToUiMapper @Inject constructor(

) {

    fun mapMessage(
        message: ChatMessage
    ): ChatMessageUi {
        return if (message.isFromUser) {
            ChatMessageUi.Outgoing(
                id = message.id,
                body = message.body,
                isRead = message.isRead
            )
        } else {
            ChatMessageUi.Incoming(
                id = message.id,
                body = message.body
            )
        }
    }

}
