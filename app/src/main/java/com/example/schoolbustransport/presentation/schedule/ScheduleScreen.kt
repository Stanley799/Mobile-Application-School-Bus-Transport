package com.example.schoolbustransport.presentation.schedule

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.schoolbustransport.domain.model.Trip
import com.example.schoolbustransport.presentation.trip.TripState
import com.example.schoolbustransport.presentation.trip.TripViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScheduleScreen(tripViewModel: TripViewModel = hiltViewModel()) {
    val tripState by tripViewModel.tripState.collectAsState()

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Full Schedule") }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text("All Scheduled Trips", style = MaterialTheme.typography.headlineSmall)

            when (val state = tripState) {
                is TripState.Loading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
                is TripState.Error -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("Error: ${state.message}", color = MaterialTheme.colorScheme.error)
                    }
                }
                is TripState.Success -> {
                    if (state.trips.isEmpty()) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text("No trips scheduled.")
                        }
                    } else {
                        LazyColumn(modifier = Modifier.fillMaxSize()) {
                            items(state.trips) { trip ->
                                TripScheduleItem(trip = trip)
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
fun TripScheduleItem(trip: Trip) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(trip.route.name, style = MaterialTheme.typography.titleLarge)
            Spacer(modifier = Modifier.height(8.dp))
            Text("Bus: ${trip.bus.licensePlate}", style = MaterialTheme.typography.bodyMedium)
            Text("Driver: ${trip.driver.name}", style = MaterialTheme.typography.bodyMedium)
            Text("Departure: ${trip.departureTime}", style = MaterialTheme.typography.bodyMedium)
            Text("Arrival: ${trip.arrivalTime}", style = MaterialTheme.typography.bodyMedium)
            Text("Status: ${trip.status}", style = MaterialTheme.typography.bodyMedium)
        }
    }
}
