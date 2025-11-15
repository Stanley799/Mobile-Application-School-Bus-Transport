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
import com.example.schoolbustransport.domain.model.UserRole
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MessagesViewModel @Inject constructor(
    private val api: ApiService,
    private val sessionManager: SessionManager
) : ViewModel() {
    private var socketManager: com.example.schoolbustransport.data.realtime.SocketManager? = null
    private var currentChatUserId: Int? = null

    fun initSocket(token: String, apiBaseUrl: String) {
        if (socketManager == null) {
            socketManager = com.example.schoolbustransport.data.realtime.SocketManager(apiBaseUrl, token)
            socketManager?.connect()
            socketManager?.on("message-broadcast") { args ->
                if (args.isNotEmpty()) {
                    val msgJson = args[0].toString()
                    try {
                        val gson = com.google.gson.Gson()
                        val msg = gson.fromJson(msgJson, com.example.schoolbustransport.data.network.dto.MessageDto::class.java)
                        if (msg.senderId == currentChatUserId || msg.receiverId == currentChatUserId) {
                            _messages.value = _messages.value + msg
                        }
                    } catch (_: Exception) {}
                }
            }
        }
    }

    val myUserId: StateFlow<Int?> = sessionManager.userIdFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = null
    )

    val userRoleFlow: StateFlow<UserRole?> = sessionManager.userRoleFlow.stateIn(
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
                val body = resp.body()
                if (resp.isSuccessful && body != null) {
                    _conversations.value = body
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

    fun loadMessages(otherUserId: Int) {
        currentChatUserId = otherUserId
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                val resp = api.getMessages(otherUserId.toString())
                val body = resp.body()
                if (resp.isSuccessful && body != null) {
                    _messages.value = body
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

    fun sendMessage(otherUserId: Int, text: String, type: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                val resp = api.sendMessage(SendMessageRequest(receiverId = otherUserId, content = text, type = type))
                if (resp.isSuccessful) {
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
                val body = resp.body()
                if (resp.isSuccessful && body != null) {
                    allRecipients = body
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
