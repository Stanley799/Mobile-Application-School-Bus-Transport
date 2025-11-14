package com.example.schoolbustransport.presentation.schedule

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.schoolbustransport.presentation.trip.TripViewModel
import com.example.schoolbustransport.domain.model.Trip
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Composable
fun ScheduleScreen(tripViewModel: TripViewModel = hiltViewModel()) {
    val tripState by tripViewModel.tripState.collectAsState()
    var selectedDate by remember { mutableStateOf(LocalDate.now()) }
    val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")

    LaunchedEffect(selectedDate) {
        tripViewModel.loadTrips(selectedDate.format(formatter))
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Schedule / Calendar") }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Date picker (simple)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text("Date:", style = MaterialTheme.typography.titleMedium)
                OutlinedTextField(
                    value = selectedDate.format(formatter),
                    onValueChange = {
                        runCatching { LocalDate.parse(it, formatter) }.onSuccess { selectedDate = it }
                    },
                    label = { Text("YYYY-MM-DD") },
                    singleLine = true,
                    modifier = Modifier.width(140.dp)
                )
            }
            Divider()
            // Trip list for selected date
            when (tripState) {
                is com.example.schoolbustransport.presentation.trip.TripState.Loading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
                is com.example.schoolbustransport.presentation.trip.TripState.Success -> {
                    val trips = (tripState as com.example.schoolbustransport.presentation.trip.TripState.Success).trips
                    if (trips.isEmpty()) {
                        Text("No trips scheduled for this date.", style = MaterialTheme.typography.bodyLarge)
                    } else {
                        LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            items(trips) { trip ->
                                Card(modifier = Modifier.fillMaxWidth()) {
                                    Column(modifier = Modifier.padding(16.dp)) {
                                        Text(trip.trip_name, style = MaterialTheme.typography.titleMedium)
                                        Text("Bus: ${trip.bus_name ?: "-"}", style = MaterialTheme.typography.bodyMedium)
                                        Text("Driver: ${trip.driver_name ?: "-"}", style = MaterialTheme.typography.bodyMedium)
                                        Text("Departure: ${trip.departure_time ?: "-"}", style = MaterialTheme.typography.bodyMedium)
                                        Text("Arrival: ${trip.arrival_time ?: "-"}", style = MaterialTheme.typography.bodyMedium)
                                        Text("Status: ${trip.status ?: "-"}", style = MaterialTheme.typography.bodyMedium)
                                    }
                                }
                            }
                        }
                    }
                }
                is com.example.schoolbustransport.presentation.trip.TripState.Error -> {
                    Text("Error loading trips: ${(tripState as com.example.schoolbustransport.presentation.trip.TripState.Error).message}", color = MaterialTheme.colorScheme.error)
                }
                else -> {}
            }
        }
    }
}
