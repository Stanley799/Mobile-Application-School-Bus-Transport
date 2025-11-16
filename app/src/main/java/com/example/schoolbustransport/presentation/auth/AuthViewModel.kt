package com.example.schoolbustransport.presentation.auth

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.schoolbustransport.domain.model.User
import com.example.schoolbustransport.domain.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    fun loginWithGoogle(idToken: String) {
        viewModelScope.launch {
            _loginState.value = LoginState.Loading
            val result = authRepository.loginWithGoogle(idToken)
            result.fold(
                onSuccess = { user ->
                    _loginState.value = LoginState.Success(user)
                },
                onFailure = { error ->
                    _loginState.value = LoginState.Error(error.message ?: "Google sign-in failed")
                }
            )
        }
    }

    fun handleGoogleSignInError(message: String = "An unexpected error occurred during Google sign-in.") {
        _loginState.value = LoginState.Error(message)
    }

    fun updateUserRole(role: String) {
        viewModelScope.launch {
            _loginState.value = LoginState.Loading
            val result = authRepository.updateUserRole(role)
            result.fold(
                onSuccess = { updatedUser ->
                    _loginState.value = LoginState.Success(updatedUser)
                },
                onFailure = { error ->
                    _loginState.value = LoginState.Error(error.message ?: "Failed to update role")
                }
            )
        }
    }

    fun updateUserProfile(phone: String, imageUri: Uri?) {
        viewModelScope.launch {
            _loginState.value = LoginState.Loading
            val result = authRepository.updateUserProfile(phone, imageUri)
            result.fold(
                onSuccess = { updatedUser ->
                    _loginState.value = LoginState.Success(updatedUser)
                },
                onFailure = { error ->
                    _loginState.value = LoginState.Error(error.message ?: "Failed to update profile")
                }
            )
        }
    }

    private val _loginState = MutableStateFlow<LoginState>(LoginState.Loading) // Start with Loading
    val loginState: StateFlow<LoginState> = _loginState

    private val _registerState = MutableStateFlow<RegisterState>(RegisterState.Idle)
    val registerState: StateFlow<RegisterState> = _registerState

    init {
        // Observe the user flow from the repository
        viewModelScope.launch {
            authRepository.getLoggedInUser().collectLatest { user ->
                _loginState.value = if (user != null) {
                    LoginState.Success(user)
                } else {
                    LoginState.Idle // No user logged in
                }
            }
        }
    }

    fun login(email: String, password: String) {
        viewModelScope.launch {
            _loginState.value = LoginState.Loading
            val result = authRepository.login(email, password)
            result.fold(
                onSuccess = { user ->
                    _loginState.value = LoginState.Success(user)
                },
                onFailure = { error ->
                    _loginState.value = LoginState.Error(error.message ?: "An unknown error occurred")
                }
            )
        }
    }

    fun register(name: String, email: String, phone: String, password: String, role: String) {
        viewModelScope.launch {
            _registerState.value = RegisterState.Loading
            val result = authRepository.register(name, email, phone, password, role)
            result.fold(
                onSuccess = { user ->
                    // Registration now automatically logs the user in, so update the login state
                    _loginState.value = LoginState.Success(user)
                    _registerState.value = RegisterState.Success(user)
                },
                onFailure = { error ->
                    _registerState.value = RegisterState.Error(error.message ?: "An unknown error occurred")
                }
            )
        }
    }

    fun logout() {
        viewModelScope.launch {
            authRepository.logout()
            _loginState.value = LoginState.Idle
        }
    }

    fun resetRegisterState() {
        _registerState.value = RegisterState.Idle
    }
}

sealed class LoginState {
    object Idle : LoginState()
    object Loading : LoginState()
    data class Success(val user: User) : LoginState()
    data class Error(val message: String) : LoginState()
}

sealed class RegisterState {
    object Idle : RegisterState()
    object Loading : RegisterState()
    data class Success(val user: User) : RegisterState()
    data class Error(val message: String) : RegisterState()
}
