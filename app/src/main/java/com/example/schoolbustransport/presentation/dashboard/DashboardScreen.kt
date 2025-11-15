package com.example.schoolbustransport.presentation.dashboard

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Message
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.NotificationCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.schoolbustransport.BuildConfig
import com.example.schoolbustransport.R
import com.example.schoolbustransport.domain.model.Trip
import com.example.schoolbustransport.domain.model.TripStatus
import com.example.schoolbustransport.domain.model.User
import com.example.schoolbustransport.domain.model.UserRole
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
    val tripViewModel: TripViewModel = hiltViewModel()
    val tripState by tripViewModel.tripState.collectAsState()
    var searchText by remember { mutableStateOf("") }
    val context = LocalContext.current
    // Load trips on first composition
    LaunchedEffect(Unit) { tripViewModel.loadTrips() }

    // Notification state
    val messagesViewModel: MessagesViewModel = hiltViewModel()
    val notificationMessage = remember { mutableStateOf<String?>(null) }
    val notificationSender = remember { mutableStateOf<String?>(null) }
    val notificationVisible = remember { mutableStateOf(false) }
    val shownNotificationIds = remember { mutableStateOf(setOf<Int>()) }

    // Listen for notification messages globally
    LaunchedEffect(Unit) {
        messagesViewModel.initSocket(authViewModel.tokenFlow.value ?: "", BuildConfig.BASE_URL)
        messagesViewModel.messages.collect { messages ->
            // Show banner and system notification for new, unread notifications
            val notif = messages.lastOrNull { 
                it.type == "notification" && !shownNotificationIds.value.contains(it.messageId)
            }
            // Only show notifications for parents
            if (user.role is UserRole.Parent && notif != null) {
                val senderName = notif.sender?.name ?: notif.senderId.toString()
                // Show in-app banner
                notificationMessage.value = notif.content
                notificationSender.value = senderName
                notificationVisible.value = true
                // Show system notification
                showHeadsUpNotification(context, "New message from $senderName", notif.content)
                // Add to seen IDs
                shownNotificationIds.value += notif.messageId
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
                            IconButton(onClick = { navController.navigate("profile") }) {
                                Icon(Icons.Default.Person, contentDescription = "Profile")
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = MaterialTheme.colorScheme.background
                        )
                    )
                }
            },
            bottomBar = { BottomNavigationBar(navController = navController, user = user) }
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
                val currentTrip = trips.firstOrNull { it.status == TripStatus.IN_PROGRESS }
                val upcomingTrips = trips.filter { it.status == TripStatus.SCHEDULED }
                // Current Trip Card
                if (currentTrip != null) {
                    CurrentTripCard(trip = currentTrip)
                } else {
                    CurrentTripCard(trip = null)
                }
                Spacer(modifier = Modifier.height(24.dp))

                // Search Bar
                OutlinedTextField(
                    value = searchText,
                    onValueChange = { searchText = it },
                    placeholder = { Text("Search for routes, schedules, or bus stops") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    trailingIcon = {
                        IconButton(onClick = { /* TODO: Implement search functionality */ }) {
                            Icon(Icons.Default.Search, contentDescription = "Search")
                        }
                    },
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

                // Upcoming Trips
                UpcomingTripsCard(upcomingTrips, modifier = Modifier.fillMaxWidth())
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // Bottom action cards
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    ActionCard(
                        title = "Live Tracking", 
                        subtitle = "View Map", 
                        modifier = Modifier.weight(1f),
                        onClick = { navController.navigate("live_tracking") } 
                    )
                    ActionCard(
                        title = "Notifications", 
                        subtitle = "Contact Support", 
                        modifier = Modifier.weight(1f), 
                        isPrimary = false,
                        onClick = { navController.navigate("notifications") }
                    )
                }
                Spacer(modifier = Modifier.height(24.dp))
                // Personalized Schedule/Trips Section
                PersonalizedTripsSection(user = user, trips = trips)
            }
        }
    }
}

@Composable
fun CurrentTripCard(trip: Trip?) {
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
            Text("Truly S:7 PI8", fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
fun UpcomingTripsCard(trips: List<Trip>, modifier: Modifier = Modifier) {
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ActionCard(title: String, subtitle: String, modifier: Modifier = Modifier, isPrimary: Boolean = true, onClick: () -> Unit) {
    val backgroundColor = if (isPrimary) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.secondaryContainer
    Card(modifier = modifier, shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(containerColor = backgroundColor), onClick = onClick) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(title, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
            Text(subtitle, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
fun BottomNavigationBar(navController: NavController, user: User) {
    NavigationBar {
        NavigationBarItem(
            icon = { Icon(Icons.Filled.Home, contentDescription = "Home") },
            label = { Text("Home") },
            selected = true, // Always selected on the dashboard
            onClick = { /* No-op, already home */ }
        )
        if (user.role is UserRole.Admin) {
            NavigationBarItem(
                icon = { Icon(Icons.Filled.CalendarToday, contentDescription = "Schedule") },
                label = { Text("Schedule") },
                selected = false,
                onClick = { navController.navigate("schedule") }
            )
        }
        NavigationBarItem(
            icon = { Icon(Icons.AutoMirrored.Filled.Message, contentDescription = "Messages") },
            label = { Text("Messages") },
            selected = false,
            onClick = { navController.navigate("messages") }
        )
    }
}

@Composable
fun PersonalizedTripsSection(user: User, trips: List<Trip>) {
    val filteredTrips = when (user.role) {
        is UserRole.Parent -> trips.filter { trip -> trip.students.any { it.parentId == user.id } }
        is UserRole.Driver -> trips.filter { trip -> trip.driver.id == user.id }
        is UserRole.Admin -> trips
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
                            Text("${trip.route.name} | ${trip.departureTime ?: "-"} ", fontWeight = FontWeight.Medium)
                            Text("Bus: ${trip.bus.licensePlate}", fontSize = 13.sp)
                            Text("Driver: ${trip.driver.name}", fontSize = 13.sp)
                            Text("Status: ${trip.status}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun NotificationBanner(
    message: String?,
    sender: String?,
    visible: Boolean,
    onDismiss: () -> Unit
) {
    if (visible && message != null) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.Yellow)
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = "New message from $sender: $message")
            IconButton(onClick = onDismiss) {
                Icon(Icons.Default.Close, contentDescription = "Dismiss")
            }
        }
    }
}

private fun showHeadsUpNotification(context: Context, title: String, text: String) {
    val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    val channelId = "heads_up_channel"

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        val channel = NotificationChannel(
            channelId, 
            "Heads-Up Notifications", 
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "High-priority notifications for important alerts."
        }
        notificationManager.createNotificationChannel(channel)
    }

    val notification = NotificationCompat.Builder(context, channelId)
        .setContentTitle(title)
        .setContentText(text)
        .setSmallIcon(R.drawable.ic_launcher_foreground)
        .setPriority(NotificationCompat.PRIORITY_HIGH)
        .setDefaults(NotificationCompat.DEFAULT_ALL)
        .setAutoCancel(true) // Dismisses when tapped
        .build()

    notificationManager.notify(2, notification)
}
