    // Registration state
    private val _registerState = MutableStateFlow<RegisterState>(RegisterState.Idle)
    val registerState: StateFlow<RegisterState> = _registerState

    fun register(name: String, email: String, phone: String, password: String, role: String) {
        viewModelScope.launch {
            _registerState.value = RegisterState.Loading
            val result = authRepository.register(name, email, phone, password, role)
            result.fold(
                onSuccess = { user -> _registerState.value = RegisterState.Success(user) },
                onFailure = { error -> _registerState.value = RegisterState.Error(error.message ?: "An unknown error occurred") }
            )
        }
    }

    fun resetRegisterState() { _registerState.value = RegisterState.Idle }

// Registration state sealed class
sealed class RegisterState {
    object Idle : RegisterState()
    object Loading : RegisterState()
    data class Success(val user: User) : RegisterState()
    data class Error(val message: String) : RegisterState()
}
package com.example.schoolbustransport.presentation.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.schoolbustransport.domain.model.User
import com.example.schoolbustransport.domain.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * AuthViewModel - Manages authentication state and operations
 * 
 * Responsibilities:
 * - Handles user login/logout operations
 * - Manages authentication state (Idle, Loading, Success, Error)
 * - Coordinates with AuthRepository for API calls
 * - Provides reactive state via StateFlow for UI observation
 * 
 * State Flow:
 * - Idle: Initial state, no authentication attempt
 * - Loading: Authentication in progress
 * - Success: User authenticated successfully (contains User object)
 * - Error: Authentication failed (contains error message)
 * 
 * @param authRepository Repository for authentication operations
 */
@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    // Private mutable state flow - only this ViewModel can update it
    private val _loginState = MutableStateFlow<LoginState>(LoginState.Idle)
    // Public read-only state flow - UI observes this
    val loginState: StateFlow<LoginState> = _loginState

    /**
     * Attempts to authenticate the user with provided credentials
     * 
     * Flow:
     * 1. Sets state to Loading
     * 2. Calls repository to perform login (includes token fetch and profile retrieval)
     * 3. Updates state to Success with user data, or Error with message
     * 
     * @param email User's email address
     * @param password User's password
     */
    fun login(email: String, password: String) {
        viewModelScope.launch {
            _loginState.value = LoginState.Loading
            // The repository handles the entire login flow:
            // - API call to authenticate
            // - Token storage via SessionManager
            // - Profile fetch using the stored token
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

    /**
     * Logs out the current user
     * 
     * Clears the authentication token from storage and resets state to Idle.
     * This allows the user to log in again or remain on the login screen.
     */
    fun logout() {
        viewModelScope.launch {
            authRepository.logout()
            _loginState.value = LoginState.Idle
        }
    }
}

/**
 * LoginState - Sealed class representing authentication states
 * 
 * Used to model the different states of the authentication process:
 * - Idle: No authentication attempt has been made
 * - Loading: Authentication request is in progress
 * - Success: Authentication succeeded (contains authenticated User)
 * - Error: Authentication failed (contains error message for display)
 */
sealed class LoginState {
    object Idle : LoginState()
    object Loading : LoginState()
    data class Success(val user: User) : LoginState()
    data class Error(val message: String) : LoginState()
}
