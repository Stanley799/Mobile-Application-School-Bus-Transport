package com.example.schoolbustransport.presentation.trip

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.schoolbustransport.domain.model.Trip

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TripListScreen(
	navController: NavController,
	tripViewModel: TripViewModel = hiltViewModel()
) {
	val tripState by tripViewModel.tripState.collectAsState()

	// The ViewModel now loads trips automatically in its init block

	Scaffold(
		topBar = {
			TopAppBar(
				title = { Text("Trips") },
				navigationIcon = {
					IconButton(onClick = { navController.popBackStack() }) {
						Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
					}
				}
			)
		}
	) { padding ->
		Box(modifier = Modifier.fillMaxSize().padding(padding)) {
			when (val state = tripState) {
				is TripState.Loading -> {
					Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
						CircularProgressIndicator()
					}
				}
				is TripState.Error -> {
					Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
						Text("Error: ${state.message}")
					}
				}
				is TripState.Success -> {
					if (state.trips.isEmpty()) {
						Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
							Text("No trips found")
						}
					} else {
						LazyColumn(
							modifier = Modifier.fillMaxSize().padding(16.dp),
							verticalArrangement = Arrangement.spacedBy(12.dp)
						) {
							items(state.trips) { trip ->
								TripRow(
									trip = trip,
									onStart = { tripViewModel.startTrip(trip.id) },
									onEnd = { tripViewModel.endTrip(trip.id) },
									onOpenMap = { navController.navigate("live_tracking?tripId=${trip.id}") },
									onOpenAttendance = { navController.navigate("attendance/${trip.id}") },
									navController = navController
								)
							}
						}
					}
				}
				else -> {}
			}
		}
	}
}

@Composable
private fun TripRow(
	trip: Trip,
	onStart: () -> Unit,
	onEnd: () -> Unit,
	onOpenMap: () -> Unit,
	onOpenAttendance: () -> Unit,
	navController: NavController
) {
	Card(
		modifier = Modifier.fillMaxWidth(),
		elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
	) {
		Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
			Text(trip.route.name, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
			Spacer(Modifier.height(4.dp))
			Text("Bus: ${trip.bus.licensePlate}", style = MaterialTheme.typography.bodyMedium)
			Text("Driver: ${trip.driver.name}", style = MaterialTheme.typography.bodyMedium)
			Text("Status: ${trip.status}", style = MaterialTheme.typography.bodyMedium)

			Spacer(Modifier.height(12.dp))
			Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
				// Start and End buttons are enabled/disabled based on current status
				Button(onClick = onStart, enabled = trip.status.name != "IN_PROGRESS") { Text("Start Trip") }
				Button(onClick = onEnd, enabled = trip.status.name == "IN_PROGRESS") { Text("End Trip") }
				TextButton(onClick = onOpenMap) { Text("Open Map") }
				TextButton(onClick = onOpenAttendance) { Text("Attendance") }
				if (trip.status.name == "COMPLETED") {
					TextButton(onClick = { navController.navigate("trip_report/${trip.id}") }) { Text("View Report") }
				}
			}
		}
	}
}
