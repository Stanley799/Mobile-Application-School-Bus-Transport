package com.example.schoolbustransport.presentation.trip

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.schoolbustransport.domain.model.Trip
import com.example.schoolbustransport.domain.repository.TripRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TripViewModel @Inject constructor(
    private val tripRepository: TripRepository
) : ViewModel() {

    private val _tripState = MutableStateFlow<TripState>(TripState.Idle)
    val tripState: StateFlow<TripState> = _tripState

    private val _selectedTrip = MutableStateFlow<Trip?>(null)
    val selectedTrip: StateFlow<Trip?> = _selectedTrip

    init {
        loadTrips()
    }

    fun loadTrips() {
        viewModelScope.launch {
            _tripState.value = TripState.Loading
            tripRepository.getTrips()
                .catch { e ->
                    _tripState.value = TripState.Error(e.message ?: "An error occurred")
                }
                .collectLatest { trips ->
                    _tripState.value = TripState.Success(trips)
                }
        }
    }

    fun loadTripDetails(tripId: String) {
        viewModelScope.launch {
            tripRepository.getTripDetails(tripId)
                .catch { /* Handle error if needed */ }
                .collectLatest { trip ->
                    _selectedTrip.value = trip
                }
        }
    }

    fun startTrip(tripId: String) {
        viewModelScope.launch {
            tripRepository.startTrip(tripId).onFailure {
                // Optionally update UI with error state
            }
        }
    }

    fun endTrip(tripId: String) {
        viewModelScope.launch {
            tripRepository.endTrip(tripId).onFailure {
                // Optionally update UI with error state
            }
        }
    }

    fun markAttendance(tripId: String, studentId: String, status: String) {
        viewModelScope.launch {
            tripRepository.markAttendance(tripId, studentId, status).onFailure {
                // Optionally update UI with error state
            }
        }
    }
}

sealed class TripState {
    object Idle : TripState()
    object Loading : TripState()
    data class Success(val trips: List<Trip>) : TripState()
    data class Error(val message: String) : TripState()
}
