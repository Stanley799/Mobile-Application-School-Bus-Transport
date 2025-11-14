package com.example.schoolbustransport.presentation.trip

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.schoolbustransport.domain.model.User
import com.example.schoolbustransport.presentation.auth.AuthViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
@Composable
fun DashboardScreen(
    user: User,
    navController: NavController,
    tripViewModel: TripViewModel = hiltViewModel()
) {
    // Observe trip state from ViewModel
    val tripState by tripViewModel.tripState.collectAsState()
    // Load trips on first composition
    LaunchedEffect(Unit) { tripViewModel.loadTrips() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Welcome, ${user.name}", fontWeight = FontWeight.Bold) },
                actions = {
                    IconButton(onClick = { navController.navigate("notifications") }) {
                        Icon(Icons.Default.Notifications, contentDescription = "Notifications")
                    }
                    IconButton(onClick = { navController.navigate("profile") }) {
                        Icon(Icons.Default.Person, contentDescription = "Profile")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        bottomBar = { BottomNavigationBar(navController = navController) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp)
        ) {
            // Search Bar (functional placeholder)
            OutlinedTextField(
                value = "",
                onValueChange = {},
                placeholder = { Text("Search for routes, trips, or students") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(32.dp)
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Current Trip Details Card (shows the first in-progress trip, if any)
            val currentTrip = (tripState as? TripState.Success)?.trips?.firstOrNull { it.status.name == "IN_PROGRESS" }
            Text("Current Trip", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(16.dp))
            if (currentTrip != null) {
                CurrentTripCard(trip = currentTrip, navController = navController)
            } else {
                Text("No active trip.", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Quick Access Row
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp), modifier = Modifier.fillMaxWidth()) {
                QuickAccessCard(
                    icon = Icons.Default.Home,
                    label = "Live Tracking",
                    onClick = { navController.navigate("live_tracking") }
                )
                QuickAccessCard(
                    icon = Icons.Default.Notifications,
                    label = "Messages",
                    onClick = { navController.navigate("messages") }
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Upcoming Trips & Bus Stops
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                UpcomingTripsCard(modifier = Modifier.weight(1f))
                BusStopsCard(modifier = Modifier.weight(1f))
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Personalized Schedule/Trips Section (role-based)
            PersonalizedTripsSection(
                user = user,
                navController = navController,
                trips = (tripState as? TripState.Success)?.trips ?: emptyList()
            )
        }
    }
}

@Composable
@Composable
fun CurrentTripCard(trip: Trip, navController: NavController) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { navController.navigate("live_tracking?tripId=${trip.id}") },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Bus: ${trip.bus.licensePlate}", fontWeight = FontWeight.Bold, fontSize = 18.sp)
            Text("Trip: ${trip.route.name}", fontSize = 14.sp)
            Text("Driver: ${trip.driver.name}", fontSize = 14.sp)
            Text("Time: ${trip.departureTime ?: "-"} - ${trip.arrivalTime ?: "-"}", fontSize = 14.sp)
            Text("Bus Details: ${trip.bus.model ?: "-"}, ${trip.bus.capacity} seats", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
fun QuickAccessCard(icon: ImageVector, label: String, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .weight(1f)
            .height(80.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(icon, contentDescription = label, modifier = Modifier.size(32.dp))
            Text(label, fontWeight = FontWeight.Medium)
        }
    }
}

@Composable
@Composable
fun PersonalizedTripsSection(user: User, navController: NavController, trips: List<Trip>) {
    Text("Your Trips", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
    Spacer(modifier = Modifier.height(8.dp))
    val filteredTrips = when (user.role) {
        is UserRole.Admin -> trips
        is UserRole.Driver -> trips.filter { it.driver.id == user.id }
        is UserRole.Parent -> trips.filter { it.students.any { s -> s.parentId == user.id } }
    }
    if (filteredTrips.isEmpty()) {
        Text("No trips found.", color = MaterialTheme.colorScheme.onSurfaceVariant)
    } else {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            filteredTrips.forEach { trip ->
                TripItemCard(
                    tripName = trip.route.name,
                    status = trip.status.name.replace("_", " ").capitalize(),
                    onClick = { navController.navigate("live_tracking?tripId=${trip.id}") }
                )
            }
        }
    }
}

@Composable
fun TripItemCard(tripName: String, status: String, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(tripName, fontWeight = FontWeight.Bold)
                Text(status, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Icon(Icons.Default.Home, contentDescription = null)
        }
    }
}

@Composable
fun UpcomingTripsCard(modifier: Modifier = Modifier) {
    Card(modifier = modifier, shape = RoundedCornerShape(16.dp)) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Upcoming Trips", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(16.dp))
            // Placeholder for timeline
            Text("2:40pm -> 11:9pm")
            Text("0:30pm -> 051pm")
        }
    }
}

@Composable
fun BusStopsCard(modifier: Modifier = Modifier) {
    Card(modifier = modifier, shape = RoundedCornerShape(16.dp)) {
        Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text("Bus Stops", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
            ) // Placeholder for mini-map
            Text("17.24+ Estimated Arrival", fontSize = 12.sp, modifier = Modifier.padding(top = 8.dp))
        }
    }
}

@Composable
fun BottomNavigationBar(navController: NavController) {
    NavigationBar {
        NavigationBarItem(
            icon = { Icon(Icons.Filled.Home, contentDescription = "Home") },
            label = { Text("Home") },
            selected = true,
            onClick = { /* No-op */ }
        )
        NavigationBarItem(
            icon = { Icon(Icons.Filled.CalendarToday, contentDescription = "Schedule") },
            label = { Text("Schedule") },
            selected = false,
            onClick = { navController.navigate("schedule") }
        )
        NavigationBarItem(
            icon = { Icon(Icons.Filled.Notifications, contentDescription = "Notifications") },
            label = { Text("Notifications") },
            selected = false,
            onClick = { navController.navigate("notifications") }
        )
        NavigationBarItem(
            icon = { Icon(Icons.Filled.Person, contentDescription = "Settings") },
            label = { Text("Settings") },
            selected = false,
            onClick = { navController.navigate("profile") }
        )
    }
}
