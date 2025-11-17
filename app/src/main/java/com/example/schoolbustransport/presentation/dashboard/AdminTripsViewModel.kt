
// AdminTripsViewModel: Handles loading and creating trips for the admin panel.
package com.example.schoolbustransport.presentation.dashboard


// AndroidX, Firebase, and project imports for ViewModel, state, and Firestore
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.schoolbustransport.domain.model.Bus
import com.example.schoolbustransport.domain.model.Route
import com.example.schoolbustransport.domain.model.Trip
import com.example.schoolbustransport.domain.model.User
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.toObjects
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject


@HiltViewModel
class AdminTripsViewModel @Inject constructor(
    private val firestore: FirebaseFirestore
) : ViewModel() {

    // State for buses, routes, drivers, loading, and error
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

    // Loads all buses, routes, and drivers from Firestore
    fun loadLists() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val busesSnapshot = firestore.collection("buses").get().await()
                _buses.value = busesSnapshot.toObjects(Bus::class.java)

                val routesSnapshot = firestore.collection("routes").get().await()
                _routes.value = routesSnapshot.toObjects(Route::class.java)

                val driversSnapshot = firestore.collection("users").whereEqualTo("role", "DRIVER").get().await()
                _drivers.value = driversSnapshot.toObjects(User::class.java)

            } catch (e: Exception) {
                _error.value = e.message
            } finally {
                _isLoading.value = false
            }
        }
    }

    // Creates a new trip in Firestore with the selected bus, route, and driver
    fun createTrip(bus: Bus, route: Route, driver: User, tripName: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val trip = Trip(
                    bus = bus,
                    route = route,
                    driver = driver,
                    // name = tripName, // Assuming Trip has a name property
                    // startTime = ... // Set start time, etc.
                )
                firestore.collection("trips").add(trip).await()
                // Optionally clear form or navigate on success
            } catch (e: Exception) {
                _error.value = e.message
            } finally {
                _isLoading.value = false
            }
        }
    }
}
