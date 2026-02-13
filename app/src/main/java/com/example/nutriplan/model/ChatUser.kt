package com.example.nutriplan.data.model

data class ChatUser(
    val id: String,
    val name: String,
    val avatar: String? = null,
    val isOnline: Boolean = false,
    val lastSeen: Long? = null,
    val bio: String? = null
)