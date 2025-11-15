package com.example.schoolbustransport.domain.repository

import com.example.schoolbustransport.data.network.dto.ConversationDto
import com.example.schoolbustransport.data.network.dto.MessageDto
import kotlinx.coroutines.flow.Flow

interface MessagesRepository {

    fun getConversations(): Flow<List<ConversationDto>>

    fun getMessages(otherUserId: String): Flow<List<MessageDto>>

    suspend fun sendMessage(receiverId: String, content: String, type: String): Result<Unit>

}
