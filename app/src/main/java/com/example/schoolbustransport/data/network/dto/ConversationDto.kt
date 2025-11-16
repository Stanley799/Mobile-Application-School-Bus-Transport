package com.example.schoolbustransport.data.network.dto

data class ConversationDto(
    val userId: String,
    val userName: String,
    val lastMessage: String?,
    val lastMessageTime: String?,
    val role: String?
)
