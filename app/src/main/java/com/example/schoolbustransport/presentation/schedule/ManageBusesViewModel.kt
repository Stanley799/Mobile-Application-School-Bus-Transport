package com.example.schoolbustransport.presentation.schedule

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.schoolbustransport.domain.model.Bus
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

@HiltViewModel
class ManageBusesViewModel @Inject constructor(
    private val firestore: FirebaseFirestore
) : ViewModel() {

    private val _buses = MutableStateFlow<List<Bus>>(emptyList())
    val buses: StateFlow<List<Bus>> = _buses

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    fun loadBuses() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                val snapshot = firestore.collection("buses").get().await()
                _buses.value = snapshot.toObjects(Bus::class.java)
            } catch (e: Exception) {
                _error.value = "Failed to load buses: ${e.message ?: "Unknown error"}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun createBus(name: String, numberPlate: String, numberOfSeats: Int) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                val bus = Bus(
                    name = name,
                    licensePlate = numberPlate,
                    numberOfSeats = numberOfSeats,
                    capacity = numberOfSeats
                )
                firestore.collection("buses").add(bus).await()
                loadBuses() // Refresh list
            } catch (e: Exception) {
                _error.value = "Failed to create bus: ${e.message ?: "Unknown error"}"
            } finally {
                _isLoading.value = false
            }
        }
    }
}
