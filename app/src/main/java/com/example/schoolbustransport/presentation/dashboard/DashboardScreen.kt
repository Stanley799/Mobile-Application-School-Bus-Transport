package com.example.schoolbustransport.presentation.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.DirectionsBus
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.schoolbustransport.domain.model.User
import com.example.schoolbustransport.presentation.auth.AuthViewModel
import com.example.schoolbustransport.presentation.trip.TripViewModel
import com.example.schoolbustransport.ui.theme.SchoolBusTransportTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    user: User,
    authViewModel: AuthViewModel = hiltViewModel(),
    navController: NavController
) {
    val tripViewModel: com.example.schoolbustransport.presentation.trip.TripViewModel = hiltViewModel()
    val tripState by tripViewModel.tripState.collectAsState()
    // Load trips on first composition
    androidx.compose.runtime.LaunchedEffect(Unit) { tripViewModel.loadTrips() }

    // Notification state (shared across pages)
    val messagesViewModel: com.example.schoolbustransport.presentation.dashboard.MessagesViewModel = hiltViewModel()
    val myRole = user.role
    val notificationMessage = remember { mutableStateOf<String?>(null) }
    val notificationSender = remember { mutableStateOf<String?>(null) }
    val notificationVisible = remember { mutableStateOf(false) }

    // Listen for notification messages globally
    LaunchedEffect(Unit) {
        messagesViewModel.initSocket(authViewModel.tokenFlow.stateIn(viewModelScope = androidx.lifecycle.viewModelScope).value ?: "", com.example.schoolbustransport.BuildConfig.BASE_URL)
        messagesViewModel.messages.collect { msgs ->
            val notif = msgs.lastOrNull { it.type == "notification" }
            if ((myRole == "ADMIN" || myRole == "DRIVER") && notif != null) {
                notificationMessage.value = notif.content
                notificationSender.value = notif.sender?.name ?: notif.senderId.toString()
                notificationVisible.value = true
            }
        }
    }

    SchoolBusTransportTheme {
        Scaffold(
            topBar = {
                Column {
                    NotificationBanner(
                        message = notificationMessage.value,
                        sender = notificationSender.value,
                        visible = notificationVisible.value,
                        onDismiss = { notificationVisible.value = false }
                    )
                    // ...existing code...
                    // Feedback analytics for admins
                    val feedbackAnalyticsViewModel: FeedbackAnalyticsViewModel = hiltViewModel()
                    val feedbacks by feedbackAnalyticsViewModel.feedbacks.collectAsState()
                    LaunchedEffect(Unit) {
                        if (user.role == "ADMIN") feedbackAnalyticsViewModel.loadAllFeedback()
                    }
                }
            },
            topBar = {
                TopAppBar(
                    title = { 
                        Column {
                            Text("Welcome to", fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("School Bus System", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                        }
                    },
                    navigationIcon = {
                        IconButton(onClick = { /* TODO: Open navigation drawer */ }) {
                            Icon(Icons.Default.Menu, contentDescription = "Menu")
                        }
                    },
                    actions = {
                        IconButton(onClick = { /* TODO: Search action */ }) {
                            Icon(Icons.Default.Search, contentDescription = "Search")
                        }
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
                    .padding(16.dp)
            ) {
                val trips = (tripState as? com.example.schoolbustransport.presentation.trip.TripState.Success)?.trips ?: emptyList()
                val currentTrip = trips.firstOrNull { it.status.name == "IN_PROGRESS" }
                val upcomingTrips = trips.filter { it.status.name == "SCHEDULED" }
                // Current Trip Card
                if (currentTrip != null) {
                    CurrentTripCard(trip = currentTrip)
                } else {
                    CurrentTripCard(trip = null)
                }
                Spacer(modifier = Modifier.height(24.dp))

                // Feedback Analytics Card (Admins only)
                var showAllFeedback by remember { mutableStateOf(false) }
                if (user.role == "ADMIN") {
                    FeedbackAnalyticsCard(
                        averageRating = feedbackAnalyticsViewModel.averageRating,
                        feedbackCount = feedbackAnalyticsViewModel.feedbackCount,
                        recentFeedback = feedbackAnalyticsViewModel.recentFeedback,
                        onViewAll = { showAllFeedback = true }
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                }
                if (showAllFeedback) {
                    androidx.compose.material3.AlertDialog(
                        onDismissRequest = { showAllFeedback = false },
                        confirmButton = {},
                        text = {
                            androidx.compose.foundation.layout.Box(Modifier.height(500.dp).width(350.dp)) {
                                AllFeedbackScreen(onBack = { showAllFeedback = false })
                            }
                        }
                    )
                }

                // Search Bar
                OutlinedTextField(
                    value = "",
                    onValueChange = {},
                    placeholder = { Text("Search for routes, schedules, or bus stops") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(32.dp)
                )

                Spacer(modifier = Modifier.height(24.dp))

                // My Buses Section
                Text("My Buses", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(16.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    items(3) { // Placeholder data
                        BusStatusCard(it)
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Upcoming Trips & Bus Stops
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    UpcomingTripsCard(upcomingTrips, modifier = Modifier.weight(1f))
                    BusStopsCard(currentTrip, modifier = Modifier.weight(1f))
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // Bottom action cards
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    ActionCard(title = "Book a Ride", subtitle = "Contact Support", modifier = Modifier.weight(1f))
                    ActionCard(title = "Notifications", subtitle = "Contact Support", modifier = Modifier.weight(1f), isPrimary = false)
                }
                Spacer(modifier = Modifier.height(24.dp))
                // Personalized Schedule/Trips Section
                PersonalizedTripsSection(user = user, trips = trips)
            }
        // --- Current Trip Card Composable ---
        @Composable
        fun CurrentTripCard(trip: com.example.schoolbustransport.domain.model.Trip?) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp)
                    .padding(vertical = 4.dp),
                shape = RoundedCornerShape(24.dp),
                elevation = CardDefaults.cardElevation(8.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.DirectionsBus, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(36.dp))
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text("Current Trip", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(4.dp))
                        if (trip != null) {
                            Text("Bus: ${trip.bus.licensePlate}", fontWeight = FontWeight.Medium)
                            Text("Driver: ${trip.driver.name}")
                            Text("Time: ${trip.departureTime ?: "-"} - ${trip.arrivalTime ?: "-"}")
                            Text("Route: ${trip.route.name}")
                        } else {
                            Text("No active trip.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun BusStatusCard(index: Int) {
    val colors = listOf(
        MaterialTheme.colorScheme.primaryContainer,
        MaterialTheme.colorScheme.secondaryContainer,
        MaterialTheme.colorScheme.tertiaryContainer
    )
    Card(
        modifier = Modifier.width(160.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = colors[index % colors.size])
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("9825-7455", fontWeight = FontWeight.Bold, fontSize = 18.sp)
            Spacer(modifier = Modifier.height(4.dp))
            Text("Tratuly S:7 PI8", fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
fun UpcomingTripsCard(trips: List<com.example.schoolbustransport.domain.model.Trip>, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.height(160.dp).padding(vertical = 4.dp),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(6.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.CalendarToday, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Upcoming Trips", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
            }
            Spacer(modifier = Modifier.height(8.dp))
            if (trips.isEmpty()) {
                Text("No upcoming trips.", color = MaterialTheme.colorScheme.onSurfaceVariant)
            } else {
                trips.take(2).forEach { trip ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.DirectionsBus, contentDescription = null, modifier = Modifier.size(18.dp), tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("${trip.departureTime ?: "-"} - ${trip.arrivalTime ?: "-"} | ${trip.route.name}", fontSize = 14.sp)
                    }
                }
            }
        }
    }
}

@Composable
fun BusStopsCard(trip: com.example.schoolbustransport.domain.model.Trip?, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.height(160.dp).padding(vertical = 4.dp),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(6.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Place, contentDescription = null, tint = Color.Green)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Bus Stops", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
            }
            Spacer(modifier = Modifier.height(8.dp))
            if (trip?.route?.waypoints?.isNotEmpty() == true) {
                val nextStop = trip.route.waypoints.first()
                Text("Next: ${nextStop.name}", fontSize = 14.sp)
                Text("ETA: --", fontSize = 12.sp, modifier = Modifier.padding(top = 8.dp))
            } else {
                Text("No stops available.", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
fun ActionCard(title: String, subtitle: String, modifier: Modifier = Modifier, isPrimary: Boolean = true) {
    val backgroundColor = if (isPrimary) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.secondaryContainer
    Card(modifier = modifier, shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(containerColor = backgroundColor)) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(title, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
            Text(subtitle, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
fun BottomNavigationBar(navController: NavController) {
    NavigationBar {
        NavigationBarItem(
            icon = { Icon(Icons.Filled.Home, contentDescription = "Home") },
            label = { Text("Home") },
            selected = true, // Always selected on the dashboard
            onClick = { /* No-op, already home */ }
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

@Composable
fun PersonalizedTripsSection(user: com.example.schoolbustransport.domain.model.User, trips: List<com.example.schoolbustransport.domain.model.Trip>) {
    val filteredTrips = when (user.role.name) {
        "PARENT" -> trips.filter { trip -> trip.students.any { it.parentId == user.id } }
        "DRIVER" -> trips.filter { trip -> trip.driver.id == user.id }
        else -> trips // ADMIN or others see all
    }
    Column {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Schedule, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.width(8.dp))
            Text("My Schedule", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        }
        Spacer(modifier = Modifier.height(8.dp))
        if (filteredTrips.isEmpty()) {
            Text("No trips found.", color = MaterialTheme.colorScheme.onSurfaceVariant)
        } else {
            filteredTrips.take(3).forEach { trip ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .clickable { /* TODO: Navigate to trip details or live tracking */ },
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(4.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.DirectionsBus, contentDescription = null, modifier = Modifier.size(20.dp), tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text("${trip.route.name} | ${trip.departureTime ?: "-"}", fontWeight = FontWeight.Medium)
                            Text("Bus: ${trip.bus.licensePlate}", fontSize = 13.sp)
                            Text("Driver: ${trip.driver.name}", fontSize = 13.sp)
                            Text("Status: ${trip.status.name}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
        }
    }
}
