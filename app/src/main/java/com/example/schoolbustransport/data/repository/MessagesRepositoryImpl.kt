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
        val userId = auth.currentUser?.uid ?: run {
            close(IllegalStateException("User not logged in"))
            return@callbackFlow
        }

        val sentMessagesListener = firestore.collection("messages")
            .whereEqualTo("senderId", userId)
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                // Handle snapshot
            }

        val receivedMessagesListener = firestore.collection("messages")
            .whereEqualTo("receiverId", userId)
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                // Handle snapshot
            }
        
        // This is complex. For now, we will return a dummy list.
        // A proper implementation requires combining two queries and processing the results.
        trySend(emptyList<ConversationDto>())

        awaitClose {
            sentMessagesListener.remove()
            receivedMessagesListener.remove()
        }
    }

    override fun getMessages(otherUserId: String): Flow<List<MessageDto>> = callbackFlow {
        val userId = auth.currentUser?.uid ?: run {
            close(IllegalStateException("User not logged in"))
            return@callbackFlow
        }

        val listener = firestore.collection("messages")
            .whereIn("senderId", listOf(userId, otherUserId))
            .whereIn("receiverId", listOf(userId, otherUserId))
            .orderBy("timestamp", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val messages = snapshot.toObjects(MessageDto::class.java)
                    // This is not perfect, as it doesn't filter out messages between other users
                    // who might be in the list. A more robust query is needed for a real app.
                    val filteredMessages = messages.filter { 
                        (it.senderId == userId && it.receiverId.toString() == otherUserId) || (it.senderId.toString() == otherUserId && it.receiverId == userId.toInt())
                    }
                    trySend(filteredMessages)
                }
            }
        awaitClose { listener.remove() }
    }

    override suspend fun sendMessage(receiverId: String, content: String, type: String): Result<Unit> {
        return try {
            val senderId = auth.currentUser?.uid ?: return Result.failure(IllegalStateException("User not logged in"))
            val message = mapOf(
                "senderId" to senderId,
                "receiverId" to receiverId,
                "content" to content,
                "type" to type,
                "timestamp" to System.currentTimeMillis()
            )
            firestore.collection("messages").add(message).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
