package com.example.schoolbustransport.presentation.trip

import android.content.Context
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.schoolbustransport.data.network.ApiService
import com.example.schoolbustransport.domain.model.Trip
import dagger.hilt.android.EntryPointAccessors
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TripListScreen(
	navController: NavController,
	tripViewModel: TripViewModel = hiltViewModel()
) {
	val tripState by tripViewModel.tripState.collectAsState()
	val context = LocalContext.current
	val scope = rememberCoroutineScope()

	LaunchedEffect(Unit) {
		// Fetch recent trips; backend applies role-based filtering via JWT
		tripViewModel.loadTrips()
	}

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
									// Navigate to the new attendance screen
									onOpenAttendance = { navController.navigate("attendance/${trip.id}") },
									onDownloadReport = {
										// Download the PDF report. For now we write to app files dir; can be moved to Downloads with SAF
										downloadReport(scope, context, trip.id)
									},
									navController = navController // Pass navController to TripRow
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

private fun downloadReport(scope: CoroutineScope, context: Context, tripId: String) {
	val api = EntryPointAccessors.fromApplication(context, ServiceEntryPoint::class.java).apiService()
	scope.launch(Dispatchers.IO) {
		val resp = api.getTripReport(tripId)
		val body = resp.body()
		if (resp.isSuccessful && body != null) {
			val pdfBytes = body.bytes()
			// Save under app files; in production use SAF to save under Downloads
			val outFile = File(context.filesDir, "trip-report-${tripId}.pdf")
			outFile.outputStream().use { it.write(pdfBytes) }
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
	onDownloadReport: () -> Unit,
	navController: NavController? = null // Pass navController for navigation
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
				TextButton(onClick = onDownloadReport) { Text("Download Report") }
				if (trip.status.name == "COMPLETED" && navController != null) {
					TextButton(onClick = { navController.navigate("trip_report/${trip.id}") }) { Text("View Report") }
				}
			}
		}
	}
}
