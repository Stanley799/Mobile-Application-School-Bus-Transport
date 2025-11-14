package com.example.schoolbustransport.presentation.dashboard

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.schoolbustransport.data.network.ApiService
import com.example.schoolbustransport.data.network.dto.ConversationDto
import com.example.schoolbustransport.data.network.dto.MessageDto
import com.example.schoolbustransport.data.network.dto.SendMessageRequest
import com.example.schoolbustransport.data.repository.SessionManager
import com.example.schoolbustransport.data.network.dto.UserLite
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Simple VM to retrieve conversations, fetch a thread, and send a message.
 * The backend enforces role-based messaging rules.
 */
@HiltViewModel
class MessagesViewModel @Inject constructor(
    private val api: ApiService,
    private val sessionManager: SessionManager
) : ViewModel() {

    // Expose the current user's ID from the session
    val myUserId: StateFlow<Int?> = sessionManager.userIdFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = null
    )

    private val _conversations = MutableStateFlow<List<ConversationDto>>(emptyList())
    val conversations: StateFlow<List<ConversationDto>> = _conversations

    private val _messages = MutableStateFlow<List<MessageDto>>(emptyList())
    val messages: StateFlow<List<MessageDto>> = _messages

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    private val _availableRecipients = MutableStateFlow<List<UserLite>>(emptyList())
    val availableRecipients: StateFlow<List<UserLite>> = _availableRecipients

    private var allRecipients: List<UserLite> = emptyList()

    fun loadConversations() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                val resp = api.getConversations()
                Log.d("MessagesViewModel", "Conversations response: ${resp.raw()}")
                if (resp.isSuccessful && resp.body() != null) {
                    _conversations.value = resp.body()!!
                } else {
                    _error.value = resp.errorBody()?.string()
                }
            } catch (e: Exception) {
                Log.e("MessagesViewModel", "Error loading conversations", e)
                _error.value = e.message
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun loadMessages(otherUserId: Int) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                val resp = api.getMessages(otherUserId.toString())
                if (resp.isSuccessful && resp.body() != null) {
                    _messages.value = resp.body()!!
                } else {
                    _error.value = resp.errorBody()?.string()
                }
            } catch (e: Exception) {
                _error.value = e.message
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun sendMessage(otherUserId: Int, text: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                val resp = api.sendMessage(SendMessageRequest(receiverId = otherUserId, content = text))
                if (resp.isSuccessful) {
                    // Re-fetch the thread to include the new message at the bottom
                    loadMessages(otherUserId)
                    onSuccess()
                } else {
                    _error.value = resp.errorBody()?.string()
                }
            } catch (e: Exception) {
                _error.value = e.message
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun loadAvailableRecipients() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                val resp = api.getAvailableRecipients()
                if (resp.isSuccessful && resp.body() != null) {
                    allRecipients = resp.body()!!
                    _availableRecipients.value = allRecipients
                } else {
                    _error.value = resp.errorBody()?.string()
                }
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
}
