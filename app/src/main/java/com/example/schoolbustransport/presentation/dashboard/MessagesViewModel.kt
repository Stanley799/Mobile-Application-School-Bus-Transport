
package com.example.schoolbustransport.presentation.dashboard
import kotlinx.coroutines.tasks.await

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.schoolbustransport.data.network.dto.ConversationDto
import com.example.schoolbustransport.data.network.dto.MessageDto
import com.example.schoolbustransport.domain.repository.MessagesRepository
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

@HiltViewModel
class MessagesViewModel @Inject constructor(
        private val firestore: com.google.firebase.firestore.FirebaseFirestore,
    private val messagesRepository: MessagesRepository,
    private val auth: FirebaseAuth
) : ViewModel() {

    private val _conversations = MutableStateFlow<List<ConversationDto>>(emptyList())
    val conversations: StateFlow<List<ConversationDto>> = _conversations

    private val _messages = MutableStateFlow<List<MessageDto>>(emptyList())
    val messages: StateFlow<List<MessageDto>> = _messages

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    private val _myUserId = MutableStateFlow<String?>(null)
    val myUserId: StateFlow<String?> = _myUserId

    init {
        _myUserId.value = auth.currentUser?.uid
        // Update when auth state changes
        auth.addAuthStateListener { firebaseAuth ->
            _myUserId.value = firebaseAuth.currentUser?.uid
        }
    }

    private val _availableRecipients = MutableStateFlow<List<com.example.schoolbustransport.data.network.dto.UserInfo>>(emptyList())
    val availableRecipients: StateFlow<List<com.example.schoolbustransport.data.network.dto.UserInfo>> = _availableRecipients

    private var allRecipients: List<com.example.schoolbustransport.data.network.dto.UserInfo> = emptyList()

    fun loadAvailableRecipients() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                val currentUserId = auth.currentUser?.uid
                val snapshot = firestore.collection("users").get().await()
                val users = snapshot.documents.mapNotNull { it.toObject(com.example.schoolbustransport.data.network.dto.UserInfo::class.java) }
                allRecipients = users.filter { it.id != currentUserId }
                _availableRecipients.value = allRecipients
            } catch (e: Exception) {
                _error.value = e.message
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun filterRecipients(query: String) {
        _availableRecipients.value = if (query.isBlank()) {
            allRecipients
        } else {
            allRecipients.filter {
                it.name.contains(query, ignoreCase = true) || it.role.contains(query, ignoreCase = true)
            }
        }
    }

    fun loadConversations() {
        viewModelScope.launch {
            _isLoading.value = true
            messagesRepository.getConversations()
                .catch { e -> _error.value = e.message }
                .collectLatest { conversations ->
                    _conversations.value = conversations
                    _isLoading.value = false
                }
        }
    }

    fun loadMessages(otherUserId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            messagesRepository.getMessages(otherUserId)
                .catch { e -> _error.value = e.message }
                .collectLatest { messages ->
                    _messages.value = messages
                    _isLoading.value = false
                }
        }
    }

    fun sendMessage(receiverId: String, content: String, type: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                messagesRepository.sendMessage(receiverId, content, type)
                    .onFailure { e ->
                        _error.value = "Failed to send message: ${e.message ?: "Unknown error"}"
                    }
                // Reload messages after sending
                loadMessages(receiverId)
            } catch (e: Exception) {
                _error.value = "Error: ${e.message ?: "Unknown error"}"
            } finally {
                _isLoading.value = false
            }
        }
    }
}
