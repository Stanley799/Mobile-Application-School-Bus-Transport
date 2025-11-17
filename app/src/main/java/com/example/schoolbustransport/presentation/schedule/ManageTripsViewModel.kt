package com.example.schoolbustransport.presentation.schedule

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.schoolbustransport.domain.model.Bus
import com.example.schoolbustransport.domain.model.Route
import com.example.schoolbustransport.domain.model.Student
import com.example.schoolbustransport.domain.model.Trip
import com.example.schoolbustransport.domain.model.TripStatus
import com.example.schoolbustransport.domain.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.Date
import javax.inject.Inject

@HiltViewModel
class ManageTripsViewModel @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth
) : ViewModel() {

    private val _buses = MutableStateFlow<List<Bus>>(emptyList())
    val buses: StateFlow<List<Bus>> = _buses

    private val _routes = MutableStateFlow<List<Route>>(emptyList())
    val routes: StateFlow<List<Route>> = _routes

    private val _drivers = MutableStateFlow<List<User>>(emptyList())
    val drivers: StateFlow<List<User>> = _drivers

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    fun loadLists() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                val busesSnapshot = firestore.collection("buses").get().await()
                _buses.value = busesSnapshot.toObjects(Bus::class.java)

                val routesSnapshot = firestore.collection("routes").get().await()
                _routes.value = routesSnapshot.toObjects(Route::class.java)

                val driversSnapshot = firestore.collection("users")
                    .whereEqualTo("role", "DRIVER")
                    .get()
                    .await()
                _drivers.value = driversSnapshot.toObjects(User::class.java)
            } catch (e: Exception) {
                _error.value = "Failed to load data: ${e.message ?: "Unknown error"}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun createTrip(
        tripName: String,
        route: Route,
        bus: Bus,
        grade: String,
        driver: User,
        departureDate: String,
        departureTime: String
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                // Load students for the selected grade
                val studentsSnapshot = firestore.collection("students")
                    .whereEqualTo("grade", grade)
                    .get()
                    .await()
                val studentIds = studentsSnapshot.documents.map { it.id }
                
                // Parse scheduled date
                val scheduledDate = try {
                    val dateFormat = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
                    val date = dateFormat.parse(departureDate)
                    if (date != null) {
                        com.google.firebase.Timestamp(date)
                    } else {
                        com.google.firebase.Timestamp.now()
                    }
                } catch (e: Exception) {
                    com.google.firebase.Timestamp.now()
                }

                // Create trip document matching Firestore structure
                val tripData = hashMapOf(
                    "tripName" to tripName,
                    "routeId" to route.id,
                    "busId" to bus.id,
                    "driverId" to driver.id,
                    "grade" to grade,
                    "status" to TripStatus.SCHEDULED.name,
                    "scheduledDate" to scheduledDate,
                    "departureTime" to departureTime,
                    "studentIds" to studentIds,
                    "createdAt" to com.google.firebase.Timestamp.now(),
                    "updatedAt" to com.google.firebase.Timestamp.now()
                )
                
                firestore.collection("trips").add(tripData).await()
                // Success - form will be cleared by the screen
            } catch (e: Exception) {
                _error.value = "Failed to create trip: ${e.message ?: "Unknown error"}"
            } finally {
                _isLoading.value = false
            }
        }
    }
}
