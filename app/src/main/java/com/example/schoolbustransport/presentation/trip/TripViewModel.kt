package com.example.schoolbustransport.presentation.trip

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.schoolbustransport.domain.model.Trip
import com.example.schoolbustransport.domain.repository.TripRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TripViewModel @Inject constructor(
	private val tripRepository: TripRepository
) : ViewModel() {

	private val _tripState = MutableStateFlow<TripState>(TripState.Idle)
	val tripState: StateFlow<TripState> = _tripState

	// Holds the currently selected trip with full details (used by attendance screen)
	private val _selectedTrip = MutableStateFlow<Trip?>(null)
	val selectedTrip: StateFlow<Trip?> = _selectedTrip

	fun loadTrips(date: String? = null) {
		viewModelScope.launch {
			_tripState.value = TripState.Loading
			tripRepository.getTrips(date)
				.catch { e ->
					_tripState.value = TripState.Error(e.message ?: "An error occurred")
				}
				.collect { trips ->
					_tripState.value = TripState.Success(trips)
				}
		}
	}
	
	// Fetches a single trip including its attendance list; used when opening the attendance screen
	fun loadTripDetails(tripId: String) {
		viewModelScope.launch {
			tripRepository.getTripDetails(tripId)
				.catch { /* Surface errors via UI if needed */ }
				.collect { trip -> _selectedTrip.value = trip }
		}
	}
	
	fun startTrip(tripId: String) {
		viewModelScope.launch {
			_tripState.value = TripState.Loading
			tripRepository.startTrip(tripId)
				.onSuccess {
					loadTrips() // Reload trips after starting
				}
				.onFailure { e ->
					_tripState.value = TripState.Error(e.message ?: "Failed to start trip")
				}
		}
	}
	
	fun endTrip(tripId: String) {
		viewModelScope.launch {
			_tripState.value = TripState.Loading
			tripRepository.endTrip(tripId)
				.onSuccess {
					loadTrips() // Reload trips after ending
				}
				.onFailure { e ->
					_tripState.value = TripState.Error(e.message ?: "Failed to end trip")
				}
		}
	}
	
	// Driver-only action; backend enforces that the student is on the trip
	fun markAttendance(tripId: String, studentId: String, status: String) {
		viewModelScope.launch {
			tripRepository.markAttendance(tripId, studentId, status)
				.onFailure { e ->
					_tripState.value = TripState.Error(e.message ?: "Failed to mark attendance")
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
