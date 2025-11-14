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

    fun loadAllFeedback() {
        // For demo: fetch feedback for all trips (in real app, backend endpoint for all feedback is better)
        // Here, you would loop over all trip IDs and aggregate feedbacks
        // For now, just fetch for a sample trip (id = "1")
        viewModelScope.launch {
            tripRepository.getTripFeedback("1").collect { list ->
                _feedbacks.update { list }
            }
        }
    }

    val averageRating: Float
        get() = if (feedbacks.value.isNotEmpty()) feedbacks.value.map { it.rating }.average().toFloat() else 0f
    val feedbackCount: Int
        get() = feedbacks.value.size
    val recentFeedback: List<TripFeedback>
        get() = feedbacks.value.sortedByDescending { it.createdAt }.take(3)
}
