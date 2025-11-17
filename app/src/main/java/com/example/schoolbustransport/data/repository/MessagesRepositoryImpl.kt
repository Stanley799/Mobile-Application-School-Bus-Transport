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
            trySend(emptyList())
            awaitClose {}
            return@callbackFlow
        }

        try {
            val conversationMap = mutableMapOf<String, ConversationDto>()
            
            // Get messages where user is sender
            val senderListener = firestore.collection("messages")
                .whereEqualTo("senderId", userId)
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .limit(50)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        android.util.Log.e("MessagesRepository", "Error loading sender conversations", error)
                        return@addSnapshotListener
                    }
                    
                    if (snapshot != null) {
                        snapshot.documents.forEach { doc ->
                            try {
                                val message = doc.toObject(MessageDto::class.java)
                                if (message != null) {
                                    val otherUserId = message.receiverId
                                    if (!otherUserId.isNullOrBlank() && otherUserId != userId) {
                                        val existing = conversationMap[otherUserId]
                                        if (existing == null || (message.timestamp != null && 
                                            (existing.lastMessageTime.isNullOrBlank() || 
                                             message.timestamp.toString() > existing.lastMessageTime))) {
                                            conversationMap[otherUserId] = ConversationDto(
                                                userId = otherUserId,
                                                userName = "User", // Will be populated from users collection
                                                lastMessage = message.content,
                                                lastMessageTime = message.timestamp?.toString() ?: "",
                                                role = null
                                            )
                                        }
                                    }
                                }
                            } catch (e: Exception) {
                                android.util.Log.e("MessagesRepository", "Error parsing message", e)
                            }
                        }
                    }
                }
            
            // Get messages where user is receiver
            val receiverListener = firestore.collection("messages")
                .whereEqualTo("receiverId", userId)
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .limit(50)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        android.util.Log.e("MessagesRepository", "Error loading receiver conversations", error)
                        return@addSnapshotListener
                    }
                    
                    if (snapshot != null) {
                        snapshot.documents.forEach { doc ->
                            try {
                                val message = doc.toObject(MessageDto::class.java)
                                if (message != null) {
                                    val otherUserId = message.senderId
                                    if (!otherUserId.isNullOrBlank() && otherUserId != userId) {
                                        val existing = conversationMap[otherUserId]
                                        if (existing == null || (message.timestamp != null && 
                                            (existing.lastMessageTime.isNullOrBlank() || 
                                             message.timestamp.toString() > existing.lastMessageTime))) {
                                            conversationMap[otherUserId] = ConversationDto(
                                                userId = otherUserId,
                                                userName = "User", // Will be populated from users collection
                                                lastMessage = message.content,
                                                lastMessageTime = message.timestamp?.toString() ?: "",
                                                role = null
                                            )
                                        }
                                    }
                                }
                            } catch (e: Exception) {
                                android.util.Log.e("MessagesRepository", "Error parsing message", e)
                            }
                        }
                        
                        // Fetch user names for all conversations
                        val userIds = conversationMap.keys.toList()
                        if (userIds.isNotEmpty()) {
                            // Fetch users individually (Firestore doesn't support whereIn on documentId easily)
                            var completedFetches = 0
                            val totalFetches = userIds.size
                            
                            userIds.forEach { userId ->
                                firestore.collection("users").document(userId)
                                    .get()
                                    .addOnSuccessListener { userDoc ->
                                        if (userDoc.exists()) {
                                            val userName = userDoc.getString("name") ?: "Unknown"
                                            conversationMap[userId]?.let { existing ->
                                                conversationMap[userId] = existing.copy(userName = userName)
                                            }
                                        }
                                        completedFetches++
                                        if (completedFetches == totalFetches) {
                                            trySend(conversationMap.values.toList())
                                        }
                                    }
                                    .addOnFailureListener { e ->
                                        android.util.Log.e("MessagesRepository", "Error loading user $userId", e)
                                        completedFetches++
                                        if (completedFetches == totalFetches) {
                                            trySend(conversationMap.values.toList())
                                        }
                                    }
                            }
                        } else {
                            trySend(emptyList())
                        }
                    }
                }
            
            awaitClose { 
                senderListener.remove()
                receiverListener.remove()
            }
        } catch (e: Exception) {
            android.util.Log.e("MessagesRepository", "Error in getConversations", e)
            trySend(emptyList())
            awaitClose {}
        }
    }

    override fun getMessages(otherUserId: String): Flow<List<MessageDto>> = callbackFlow {
        val userId = auth.currentUser?.uid ?: run {
            trySend(emptyList())
            awaitClose {}
            return@callbackFlow
        }

        if (otherUserId.isBlank()) {
            trySend(emptyList())
            awaitClose {}
            return@callbackFlow
        }

        try {
            // Firestore doesn't support multiple whereIn clauses, so we use OR queries
            // Query 1: messages where user is sender and otherUser is receiver
            val query1 = firestore.collection("messages")
                .whereEqualTo("senderId", userId)
                .whereEqualTo("receiverId", otherUserId)
                .orderBy("timestamp", Query.Direction.ASCENDING)
            
            // Query 2: messages where otherUser is sender and user is receiver
            val query2 = firestore.collection("messages")
                .whereEqualTo("senderId", otherUserId)
                .whereEqualTo("receiverId", userId)
                .orderBy("timestamp", Query.Direction.ASCENDING)
            
            var messages1 = emptyList<MessageDto>()
            var messages2 = emptyList<MessageDto>()
            var listener1: com.google.firebase.firestore.ListenerRegistration? = null
            var listener2: com.google.firebase.firestore.ListenerRegistration? = null
            
            fun combineAndSend() {
                val combined = (messages1 + messages2).distinctBy { 
                    it.senderId + it.receiverId + (it.timestamp?.seconds ?: 0L).toString()
                }.sortedBy { it.timestamp?.seconds ?: 0L }
                trySend(combined)
            }
            
            listener1 = query1.addSnapshotListener { snapshot, error ->
                if (error != null) {
                    android.util.Log.e("MessagesRepository", "Error loading messages (query1)", error)
                    messages1 = emptyList()
                    combineAndSend()
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    try {
                        messages1 = snapshot.documents.mapNotNull { doc ->
                            try {
                                doc.toObject(MessageDto::class.java)
                            } catch (e: Exception) {
                                android.util.Log.e("MessagesRepository", "Error parsing message", e)
                                null
                            }
                        }
                        combineAndSend()
                    } catch (e: Exception) {
                        android.util.Log.e("MessagesRepository", "Error processing messages", e)
                        messages1 = emptyList()
                        combineAndSend()
                    }
                }
            }
            
            listener2 = query2.addSnapshotListener { snapshot, error ->
                if (error != null) {
                    android.util.Log.e("MessagesRepository", "Error loading messages (query2)", error)
                    messages2 = emptyList()
                    combineAndSend()
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    try {
                        messages2 = snapshot.documents.mapNotNull { doc ->
                            try {
                                doc.toObject(MessageDto::class.java)
                            } catch (e: Exception) {
                                android.util.Log.e("MessagesRepository", "Error parsing message", e)
                                null
                            }
                        }
                        combineAndSend()
                    } catch (e: Exception) {
                        android.util.Log.e("MessagesRepository", "Error processing messages", e)
                        messages2 = emptyList()
                        combineAndSend()
                    }
                }
            }
            
            awaitClose { 
                listener1?.remove()
                listener2?.remove()
            }
        } catch (e: Exception) {
            android.util.Log.e("MessagesRepository", "Error in getMessages", e)
            trySend(emptyList())
            awaitClose {}
        }
    }

    override suspend fun sendMessage(receiverId: String, content: String, type: String): Result<Unit> {
        return try {
            val senderId = auth.currentUser?.uid ?: return Result.failure(IllegalStateException("User not logged in"))
            
            if (receiverId.isBlank()) {
                return Result.failure(IllegalArgumentException("Receiver ID cannot be blank"))
            }
            
            if (content.isBlank()) {
                return Result.failure(IllegalArgumentException("Message content cannot be blank"))
            }
            
            val timestamp = com.google.firebase.Timestamp.now()
            val messageData = hashMapOf<String, Any>(
                "senderId" to senderId,
                "receiverId" to receiverId,
                "content" to content,
                "type" to type,
                "timestamp" to timestamp,
                "read" to false
            )
            
            firestore.collection("messages").add(messageData).await()
            Result.success(Unit)
        } catch (e: Exception) {
            android.util.Log.e("MessagesRepository", "Error sending message", e)
            Result.failure(e)
        }
    }
}
