package me.squeezymo.usersupport.impl.network.dto

import com.google.firebase.Timestamp

internal data class ChatMessageDTO(
    val id: String,
    val body: String,
    val isRead: Boolean,
    val senderId: String,
    val date: Timestamp
)
