package com.example.schoolbustransport.presentation.trip

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AttendanceScreen(
	navController: NavController,
	tripId: String,
	tripViewModel: TripViewModel = hiltViewModel()
) {
	// Load the trip details (includes attendance list). Backend will filter by role.
	LaunchedEffect(tripId) {
		tripViewModel.loadTripDetails(tripId)
	}

	val trip by tripViewModel.selectedTrip.collectAsState()

	Scaffold(
		topBar = {
			TopAppBar(
				title = { Text("Attendance") },
				navigationIcon = {
					IconButton(onClick = { navController.popBackStack() }) {
						Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
					}
				}
			)
		}
	) { padding ->
		Box(Modifier.fillMaxSize().padding(padding)) {
			// Capture the trip value in a local variable to enable smart casting
			// This is necessary because 'trip' is a delegated property from collectAsState()
			val currentTrip = trip
			when {
				currentTrip == null -> {
					Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
						CircularProgressIndicator()
					}
				}
				currentTrip.students.isEmpty() -> {
					Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
						Text("No students on this trip")
					}
				}
				else -> {
					LazyColumn(
						modifier = Modifier.fillMaxSize().padding(16.dp),
						verticalArrangement = Arrangement.spacedBy(12.dp)
					) {
						items(currentTrip.students) { student ->
							Card(
								modifier = Modifier.fillMaxWidth(),
								elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
							) {
								Row(
									modifier = Modifier.fillMaxWidth().padding(12.dp),
									horizontalArrangement = Arrangement.SpaceBetween,
									verticalAlignment = Alignment.CenterVertically
								) {
									Text(student.name, style = MaterialTheme.typography.titleMedium)
									Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
										Button(onClick = { tripViewModel.markAttendance(tripId, student.id, "PRESENT") }) {
											Text("Present")
										}
										OutlinedButton(onClick = { tripViewModel.markAttendance(tripId, student.id, "ABSENT") }) {
											Text("Absent")
										}
									}
								}
							}
						}
					}
				}
			}
		}
	}
}
