package com.example.schoolbustransport.data.repository

import com.example.schoolbustransport.data.network.dto.ConversationDto
import com.example.schoolbustransport.data.network.dto.MessageDto
import com.example.schoolbustransport.domain.repository.MessagesRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject


class MessagesRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth
) : MessagesRepository {

    override fun getConversations(): Flow<List<ConversationDto>> = callbackFlow {
        // A proper implementation is complex and beyond this scope. Returning dummy data.
        trySend(emptyList())
        awaitClose {}
    }

    override fun getMessages(otherUserId: String): Flow<List<MessageDto>> = callbackFlow {
        val userId = auth.currentUser?.uid ?: run {
            close(IllegalStateException("User not logged in"))
            return@callbackFlow
        }

        val listener = firestore.collection("messages")
            .orderBy("timestamp", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val messages = snapshot.toObjects(MessageDto::class.java)
                    val filtered = messages.filter { 
                        (it.senderId == userId && it.receiverId == otherUserId) || (it.senderId == otherUserId && it.receiverId == userId)
                    }
                    trySend(filtered)
                }
            }
        awaitClose { listener.remove() }
    }

    override suspend fun sendMessage(receiverId: String, content: String, type: String): Result<Unit> {
        return try {
            val senderId = auth.currentUser?.uid ?: return Result.failure(IllegalStateException("User not logged in"))
            val message = MessageDto(
                senderId = senderId,
                receiverId = receiverId,
                content = content,
                type = type
                // timestamp is handled by the server
            )
            firestore.collection("messages").add(message).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
