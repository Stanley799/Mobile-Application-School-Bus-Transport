package com.example.schoolbustransport.presentation.trip

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.schoolbustransport.domain.model.TripFeedback
import com.example.schoolbustransport.domain.repository.TripRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TripFeedbackViewModel @Inject constructor(
    private val tripRepository: TripRepository
) : ViewModel() {

    private val _feedbackList = MutableStateFlow<List<TripFeedback>>(emptyList())
    val feedbackList: StateFlow<List<TripFeedback>> = _feedbackList

    private val _submitState = MutableStateFlow<SubmitFeedbackState>(SubmitFeedbackState.Idle)
    val submitState: StateFlow<SubmitFeedbackState> = _submitState

    fun loadTripFeedback(tripId: String) {
        viewModelScope.launch {
            tripRepository.getTripFeedback(tripId)
                .catch { _feedbackList.value = emptyList() }
                .collect { feedbacks -> _feedbackList.value = feedbacks }
        }
    }

    fun submitFeedback(tripId: String, rating: Int, comment: String?, studentId: Int? = null) {
        viewModelScope.launch {
            _submitState.value = SubmitFeedbackState.Loading
            val result = tripRepository.submitTripFeedback(tripId, rating, comment, studentId)
            _submitState.value = if (result.isSuccess) SubmitFeedbackState.Success else SubmitFeedbackState.Error(result.exceptionOrNull()?.message ?: "Failed to submit feedback")
        }
    }
}

sealed class SubmitFeedbackState {
    object Idle : SubmitFeedbackState()
    object Loading : SubmitFeedbackState()
    object Success : SubmitFeedbackState()
    data class Error(val message: String) : SubmitFeedbackState()
}
