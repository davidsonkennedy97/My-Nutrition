package com.example.nutriplan.model

data class Conversation(
    val id: String = "",
    val userId: String = "",
    val nutritionistId: String = "",
    val participantName: String = "",
    val lastMessage: String = "",
    val lastMessageTime: Long = 0,
    val lastMessageTimestamp: Long = 0,
    val unreadCount: Int = 0,
    val isOnline: Boolean = false,
    val isPinned: Boolean = false,
    val isArchived: Boolean = false
)
