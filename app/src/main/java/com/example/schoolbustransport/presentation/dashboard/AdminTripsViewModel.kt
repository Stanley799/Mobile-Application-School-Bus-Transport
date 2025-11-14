package com.example.schoolbustransport.presentation.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.schoolbustransport.data.network.ApiService
import com.example.schoolbustransport.data.network.dto.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Admin scheduling VM: loads selectable lists and posts create-trip requests.
 */
@HiltViewModel
class AdminTripsViewModel @Inject constructor(
	private val api: ApiService
) : ViewModel() {
	val buses: StateFlow<List<AdminBusItem>> get() = _buses
	private val _buses = MutableStateFlow<List<AdminBusItem>>(emptyList())

	val routes: StateFlow<List<AdminRouteItem>> get() = _routes
	private val _routes = MutableStateFlow<List<AdminRouteItem>>(emptyList())

	val drivers: StateFlow<List<DriverLiteDto>> get() = _drivers
	private val _drivers = MutableStateFlow<List<DriverLiteDto>>(emptyList())

	val isLoading: StateFlow<Boolean> get() = _loading
	private val _loading = MutableStateFlow(false)

	val error: StateFlow<String?> get() = _error
	private val _error = MutableStateFlow<String?>(null)

	fun loadLists() {
		viewModelScope.launch {
			_loading.value = true
			_error.value = null
			try {
				val busesResp = api.listBuses()
				val routesResp = api.listRoutes()
				val driversResp = api.listDrivers()
				if (busesResp.isSuccessful && routesResp.isSuccessful && driversResp.isSuccessful) {
					_buses.value = busesResp.body() ?: emptyList()
					_routes.value = routesResp.body() ?: emptyList()
					_drivers.value = driversResp.body() ?: emptyList()
				} else {
					_error.value = listOfNotNull(
						busesResp.errorBody()?.string(),
						routesResp.errorBody()?.string(),
						driversResp.errorBody()?.string()
					).firstOrNull() ?: "Failed to load admin lists"
				}
			} catch (e: Exception) {
				_error.value = e.message
			} finally {
				_loading.value = false
			}
		}
	}

	fun createTrip(busId: Int, routeId: Int, driverId: Int, tripName: String, tripDateIso: String?, onSuccess: () -> Unit) {
		viewModelScope.launch {
			_loading.value = true
			_error.value = null
			try {
				val resp = api.createTrip(CreateTripRequest(
					busId = busId,
					routeId = routeId,
					driverId = driverId,
					tripName = tripName.ifBlank { null },
					tripDate = tripDateIso
				))
				if (resp.isSuccessful) onSuccess() else _error.value = resp.errorBody()?.string()
			} catch (e: Exception) {
				_error.value = e.message
			} finally {
				_loading.value = false
			}
		}
	}
}
