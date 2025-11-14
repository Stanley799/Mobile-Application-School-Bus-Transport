package com.example.schoolbustransport.presentation.common

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.schoolbustransport.data.repository.SessionManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

/**
 * A shared ViewModel to provide session-specific data, like the auth token,
 * to different parts of the UI in a lifecycle-aware manner.
 */
@HiltViewModel
class SessionViewModel @Inject constructor(
    sessionManager: SessionManager
) : ViewModel() {

    // Expose the token as a StateFlow so that UI components can react to its changes.
    val tokenFlow: StateFlow<String?> = sessionManager.tokenFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = null
    )
}
