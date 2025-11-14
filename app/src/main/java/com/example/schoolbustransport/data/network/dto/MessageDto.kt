package com.example.schoolbustransport.data.network.dto

import com.google.gson.annotations.SerializedName

data class MessageDto(
    @SerializedName("message_id") val messageId: Int,
    @SerializedName("sender_id") val senderId: Int,
    @SerializedName("receiver_id") val receiverId: Int,
    val content: String,
    val type: String? = null,
    val timestamp: String,
    val sender: UserInfo?,
    val receiver: UserInfo?
)

data class UserInfo(
    val id: Int,
    val name: String,
    val role: String
)

data class SendMessageRequest(
    @SerializedName("receiverId") val receiverId: Int,
    val content: String,
    val type: String? = null
)

data class ConversationDto(
    @SerializedName("userId") val userId: Int,
    @SerializedName("userName") val userName: String,
    @SerializedName("userRole") val userRole: String,
    @SerializedName("lastMessage") val lastMessage: String,
    @SerializedName("lastMessageTime") val lastMessageTime: String,
    @SerializedName("unreadCount") val unreadCount: Int = 0
)

