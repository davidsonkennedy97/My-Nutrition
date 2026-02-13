package com.example.nutriplan.model

data class Conversation(
    val id: String = "",
    val userId: String = "",
    val nutritionistId: String = "",
    val lastMessage: String = "",
    val lastMessageTimestamp: Long = 0,
    val unreadCount: Int = 0
)
