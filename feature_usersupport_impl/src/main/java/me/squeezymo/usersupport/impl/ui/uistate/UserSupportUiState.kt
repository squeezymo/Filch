package me.squeezymo.usersupport.impl.ui.uistate

import me.squeezymo.usersupport.impl.ui.viewobject.ChatMessageUi

internal sealed class UserSupportUiState {

    object Loading : UserSupportUiState()

    data class Chat(
        val messages: List<ChatMessageUi>
    ) : UserSupportUiState()

}
