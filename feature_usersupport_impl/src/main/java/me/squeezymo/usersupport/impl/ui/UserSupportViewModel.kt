package me.squeezymo.usersupport.impl.ui

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import me.squeezymo.core.ext.collectTo
import me.squeezymo.core.ui.BaseViewModel
import me.squeezymo.core.ui.IBaseViewModel
import me.squeezymo.usersupport.api.domain.usecase.IMarkAllChatMessagesAsReadChatUC
import me.squeezymo.usersupport.api.domain.usecase.IReceiveMessagesFromChatUC
import me.squeezymo.usersupport.api.domain.usecase.ISendMessageToChatUC
import me.squeezymo.usersupport.impl.ui.event.UserSupportEvent
import me.squeezymo.usersupport.impl.ui.mapper.ChatDataToUiMapper
import me.squeezymo.usersupport.impl.ui.uistate.UserSupportUiState
import me.squeezymo.usersupport.impl.ui.viewobject.ChatMessageUi
import javax.inject.Inject

internal interface IUserSupportViewModel : IBaseViewModel {

    val uiState: StateFlow<UserSupportUiState>

    val isSendingMessage: StateFlow<Boolean>

    val userSupportEvent: Flow<UserSupportEvent?>

    fun submitMessage(message: String)

    fun markAllMessagesAsRead()

    fun notifyOnUserSupportEventHandled()

}

@HiltViewModel
internal class UserSupportViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val receiveMessagesFromChatUC: IReceiveMessagesFromChatUC,
    private val sendMessageToChatUC: ISendMessageToChatUC,
    private val markAllChatMessagesAsReadUC: IMarkAllChatMessagesAsReadChatUC,
    private val mapper: ChatDataToUiMapper
) : BaseViewModel(savedStateHandle), IUserSupportViewModel {

    override val uiState: MutableStateFlow<UserSupportUiState> =
        MutableStateFlow(UserSupportUiState.Loading)

    override val isSendingMessage: MutableStateFlow<Boolean> =
        MutableStateFlow(false)

    // TODO Consider using queue of events. Single event can be overwritten before being consumed
    override val userSupportEvent: MutableStateFlow<UserSupportEvent?> =
        MutableStateFlow(null)

    init {
        receiveMessages()
    }

    private fun receiveMessages() {
        viewModelScope.launch {
            launch(Dispatchers.IO) {
                receiveMessagesFromChatUC
                    .receiveMessages()
                    .map { messages ->
                        UserSupportUiState.Chat(
                            messages = ArrayList<ChatMessageUi>(messages.size + 1).also { uiMessages ->
                                uiMessages.add(ChatMessageUi.Technical.Predefined.Header)
                                messages.mapTo(uiMessages, mapper::mapMessage)
                            }
                        )
                    }
                    .collectTo(uiState)
            }
        }
    }

    override fun submitMessage(message: String) {
        viewModelScope.launch {
            launch(Dispatchers.IO) {
                isSendingMessage.value = true

                try {
                    sendMessageToChatUC.sendMessage(message)
                    userSupportEvent.value = UserSupportEvent.ClearNewMessageText
                } finally {
                    isSendingMessage.value = false
                }
            }
        }
    }

    override fun markAllMessagesAsRead() {
        viewModelScope.launch {
            launch(Dispatchers.IO) {
                markAllChatMessagesAsReadUC.markAllMessagesAsRead()
            }
        }
    }

    override fun notifyOnUserSupportEventHandled() {
        userSupportEvent.value = null
    }

}
