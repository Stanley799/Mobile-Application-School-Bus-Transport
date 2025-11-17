package com.example.schoolbustransport.presentation.schedule

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.schoolbustransport.domain.model.Trip
import com.example.schoolbustransport.domain.model.TripStatus
import com.example.schoolbustransport.presentation.trip.TripViewModel
import com.example.schoolbustransport.presentation.trip.TripState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScheduleScreen(navController: NavController) {
    val tripViewModel: TripViewModel = hiltViewModel()
    val tripState by tripViewModel.tripState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("My Schedule") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        when (val state = tripState) {
            is TripState.Loading -> {
                Box(modifier = Modifier.fillMaxSize().padding(paddingValues), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
            is TripState.Error -> {
                Box(modifier = Modifier.fillMaxSize().padding(paddingValues), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Error loading trips", color = MaterialTheme.colorScheme.error)
                        Text(state.message)
                    }
                }
            }
            is TripState.Success -> {
                val trips = state.trips.sortedByDescending { it.startTime }
                if (trips.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize().padding(paddingValues), contentAlignment = Alignment.Center) {
                        Text("No trips found", style = MaterialTheme.typography.bodyLarge)
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(paddingValues),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(trips) { trip ->
                            TripScheduleCard(trip = trip, onClick = {
                                navController.navigate("live_tracking?tripId=${trip.id}")
                            })
                        }
                    }
                }
            }
            else -> {
                Box(modifier = Modifier.fillMaxSize().padding(paddingValues), contentAlignment = Alignment.Center) {
                    Text("No trips available")
                }
            }
        }
    }
}

@Composable
fun TripScheduleCard(trip: Trip, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(4.dp),
        colors = CardDefaults.cardColors(
            containerColor = when (trip.status) {
                TripStatus.IN_PROGRESS -> MaterialTheme.colorScheme.primaryContainer
                TripStatus.COMPLETED -> MaterialTheme.colorScheme.secondaryContainer
                TripStatus.SCHEDULED -> MaterialTheme.colorScheme.surfaceVariant
                TripStatus.CANCELLED -> MaterialTheme.colorScheme.errorContainer
            }
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.DirectionsBus,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        trip.tripName.ifBlank { trip.id.ifBlank { "Trip ${trip.route.name}" } },
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("Route: ${trip.route.name}", style = MaterialTheme.typography.bodyMedium)
                    Text("Bus: ${trip.bus.licensePlate}", style = MaterialTheme.typography.bodySmall)
                    Text("Driver: ${trip.driver.name}", style = MaterialTheme.typography.bodySmall)
                    Text(
                        "Time: ${trip.departureTime ?: "-"}",
                        style = MaterialTheme.typography.bodySmall
                    )
                    Text(
                        "Status: ${trip.status.name}",
                        style = MaterialTheme.typography.bodySmall,
                        color = when (trip.status) {
                            TripStatus.IN_PROGRESS -> MaterialTheme.colorScheme.primary
                            TripStatus.COMPLETED -> MaterialTheme.colorScheme.secondary
                            TripStatus.SCHEDULED -> MaterialTheme.colorScheme.onSurfaceVariant
                            TripStatus.CANCELLED -> MaterialTheme.colorScheme.error
                        }
                    )
                    if (trip.students.isNotEmpty()) {
                        Text(
                            "Students: ${trip.students.size}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}
