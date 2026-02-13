package com.example.nutriplan.data.model

data class Conversation(
    val id: String,
    val participantName: String,
    val participantAvatar: String? = null,
    val lastMessage: String,
    val lastMessageTime: Long,
    val unreadCount: Int = 0,
    val isOnline: Boolean = false,
    val isArchived: Boolean = false,
    val isPinned: Boolean = false
)