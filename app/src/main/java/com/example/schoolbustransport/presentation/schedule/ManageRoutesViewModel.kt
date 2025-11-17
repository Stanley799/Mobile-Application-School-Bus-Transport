package com.example.schoolbustransport.presentation.schedule

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.schoolbustransport.domain.model.Route
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

@HiltViewModel
class ManageRoutesViewModel @Inject constructor(
    private val firestore: FirebaseFirestore
) : ViewModel() {

    private val _routes = MutableStateFlow<List<Route>>(emptyList())
    val routes: StateFlow<List<Route>> = _routes

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    fun loadRoutes() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                val snapshot = firestore.collection("routes").get().await()
                _routes.value = snapshot.toObjects(Route::class.java)
            } catch (e: Exception) {
                _error.value = "Failed to load routes: ${e.message ?: "Unknown error"}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun createRoute(name: String, from: String, to: String, distance: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                val route = Route(
                    name = name,
                    from = from,
                    to = to,
                    distance = distance
                )
                firestore.collection("routes").add(route).await()
                loadRoutes() // Refresh list
            } catch (e: Exception) {
                _error.value = "Failed to create route: ${e.message ?: "Unknown error"}"
            } finally {
                _isLoading.value = false
            }
        }
    }
}
