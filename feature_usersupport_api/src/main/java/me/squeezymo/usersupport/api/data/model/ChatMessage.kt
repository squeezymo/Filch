package me.squeezymo.usersupport.api.data.model

data class ChatMessage(
    val id: String,
    val body: String,
    val isRead: Boolean,
    val isFromUser: Boolean,
    val timestampMillis: Long,
    val isFromCache: Boolean
)
