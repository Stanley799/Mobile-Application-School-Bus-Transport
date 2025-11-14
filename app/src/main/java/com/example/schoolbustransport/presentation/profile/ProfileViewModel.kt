package com.example.schoolbustransport.presentation.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.schoolbustransport.domain.model.User
import com.example.schoolbustransport.domain.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val userRepository: UserRepository
) : ViewModel() {
    private val _user = MutableStateFlow<User?>(null)
    val user: StateFlow<User?> = _user

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    fun loadUser(userId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            userRepository.getUserProfile(userId).collect {
                _user.value = it
                _isLoading.value = false
            }
        }
    }

    fun updateProfile(userId: String, name: String?, phone: String?) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            val result = userRepository.updateUserProfile(userId, name, phone)
            result.fold(
                onSuccess = { _user.value = it },
                onFailure = { _error.value = it.message }
            )
            _isLoading.value = false
        }
    }

    fun uploadProfileImage(userId: String, imageFile: java.io.File) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            val result = userRepository.uploadProfileImage(userId, imageFile)
            result.fold(
                onSuccess = { _user.value = it },
                onFailure = { _error.value = it.message }
            )
            _isLoading.value = false
        }
    }

    fun deleteProfileImage(userId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            val result = userRepository.deleteProfileImage(userId)
            result.fold(
                onSuccess = { _user.value = _user.value?.copy(image = null) },
                onFailure = { _error.value = it.message }
            )
            _isLoading.value = false
        }
    }

    fun deleteAccount(userId: String) {
        // TODO: Implement account deletion logic
    }
}