package com.example.schoolbustransport.presentation.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.schoolbustransport.domain.model.TripFeedback
import com.example.schoolbustransport.domain.repository.TripRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FeedbackAnalyticsViewModel @Inject constructor(
    private val tripRepository: TripRepository
) : ViewModel() {
    private val _feedbacks = MutableStateFlow<List<TripFeedback>>(emptyList())
    val feedbacks: StateFlow<List<TripFeedback>> = _feedbacks

    /**
     * Load feedback for a specific trip.
     * Note: In production, consider adding a backend endpoint to fetch all feedback across trips.
     * 
     * @param tripId The trip ID to fetch feedback for. If null, this method does nothing.
     */
    fun loadFeedbackForTrip(tripId: String?) {
        if (tripId.isNullOrBlank()) return
        viewModelScope.launch {
            try {
                tripRepository.getTripFeedback(tripId).collect { list ->
                    _feedbacks.update { list }
                }
            } catch (e: Exception) {
                // Error handling - could emit error state if needed
                _feedbacks.update { emptyList() }
            }
        }
    }
    
    @Deprecated("Use loadFeedbackForTrip(tripId) instead. This method uses hardcoded trip ID.")
    fun loadAllFeedback() {
        loadFeedbackForTrip("1")
    }

    val averageRating: Float
        get() = if (feedbacks.value.isNotEmpty()) feedbacks.value.map { it.rating }.average().toFloat() else 0f
    val feedbackCount: Int
        get() = feedbacks.value.size
    val recentFeedback: List<TripFeedback>
        get() = feedbacks.value.sortedByDescending { it.createdAt }.take(3)
}
