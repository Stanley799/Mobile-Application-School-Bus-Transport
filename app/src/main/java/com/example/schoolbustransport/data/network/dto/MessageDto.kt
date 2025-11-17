package com.example.schoolbustransport.data.network.dto

/**
 * Represents a single message. Now Firestore-compatible.
 */
data class MessageDto(
    val id: String = "",
    val senderId: String = "",
    val receiverId: String = "",
    val content: String = "",
    val type: String? = null, // 'chat' or 'notification'
    val timestamp: com.google.firebase.Timestamp? = null, // Use Firestore Timestamp
    val read: Boolean = false,
    // The sender/receiver info can be fetched separately from the 'users' collection if needed
    // For simplicity in the message object, we will omit the nested UserInfo for now.
    val sender: UserInfo? = null,
    val receiver: UserInfo? = null
)

data class UserInfo(
    val id: String = "",
    val name: String = "",
    val role: String = ""
)

/**
 * Represents a conversation view. This is a computed object, not stored directly in Firestore.
 */
