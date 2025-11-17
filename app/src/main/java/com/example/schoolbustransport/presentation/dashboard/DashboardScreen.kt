
// DashboardScreen: Main landing page for users, showing trips, quick actions, and personalized content.
package com.example.schoolbustransport.presentation.dashboard


// Compose, Android, and project imports for UI, state, navigation, and theming
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Message
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.NotificationCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.example.schoolbustransport.R
import com.example.schoolbustransport.domain.model.Trip
import com.example.schoolbustransport.domain.model.TripStatus
import com.example.schoolbustransport.domain.model.User
import com.example.schoolbustransport.domain.model.UserRole
import com.example.schoolbustransport.presentation.trip.TripViewModel
import com.example.schoolbustransport.ui.theme.SchoolBusTransportTheme


// Main composable for the dashboard screen
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    user: User,
    navController: NavController
) {
    // ViewModel for trip data
    val tripViewModel: TripViewModel = hiltViewModel()
    val tripState by tripViewModel.tripState.collectAsState()
    var searchText by remember { mutableStateOf("") }

    SchoolBusTransportTheme {
        Scaffold(
            topBar = {
                // App bar with welcome and profile
                TopAppBar(
                    title = {
                        Column {
                            Text("Welcome to", fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("School Bus System", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                        }
                    },
                    actions = {
                        ProfileAvatar(imageUrl = user.image) {
                            navController.navigate("profile")
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.background
                    )
                )
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
                // Extract trips from state
                val trips = (tripState as? com.example.schoolbustransport.presentation.trip.TripState.Success)?.trips ?: emptyList()
                // Find current and upcoming trips
                val currentTrip = trips.firstOrNull { it.status == TripStatus.IN_PROGRESS }
                    ?: trips.sortedByDescending { it.startTime }.firstOrNull() // Show latest trip if no active trip
                val upcomingTrips = trips.filter { it.status == TripStatus.SCHEDULED }

                // Show current trip card
                if (currentTrip != null) {
                    CurrentTripCard(trip = currentTrip)
                } else {
                    CurrentTripCard(trip = null)
                }
                Spacer(modifier = Modifier.height(24.dp))

                // Search field for routes, schedules, or bus stops
                OutlinedTextField(
                    value = searchText,
                    onValueChange = { searchText = it },
                    placeholder = { Text("Search for routes, schedules, or bus stops") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(32.dp)
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Section for user-specific actions
                Text("My Space", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(16.dp))

                // Show role-specific quick actions
                MySpaceSection(user = user, navController = navController, trips = trips)

                Spacer(modifier = Modifier.height(24.dp))

                // Upcoming trips card
                UpcomingTripsCard(upcomingTrips, modifier = Modifier.fillMaxWidth())

                Spacer(modifier = Modifier.height(24.dp))

                // Quick action cards for live tracking and notifications
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

                // Personalized trip schedule section
                PersonalizedTripsSection(user = user, trips = trips)
            }
        }
    }
}


// Shows the user's profile avatar or a placeholder
@Composable
fun ProfileAvatar(imageUrl: String?, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(40.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.secondaryContainer)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        if (!imageUrl.isNullOrBlank()) {
            Image(
                painter = rememberAsyncImagePainter(imageUrl),
                contentDescription = "Profile Picture",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        } else {
            Icon(
                imageVector = Icons.Default.Person,
                contentDescription = "Profile Picture Placeholder",
                tint = MaterialTheme.colorScheme.onSecondaryContainer
            )
        }
    }
}


// Card displaying the current trip details, or a placeholder if none
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
                    Text(
                        trip.tripName.ifBlank { trip.id.ifBlank { "Trip ${trip.route.name}" } },
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleSmall
                    )
                    Text("Bus: ${trip.bus.licensePlate}", fontWeight = FontWeight.Medium)
                    Text("Driver: ${trip.driver.name}")
                    Text("Time: ${trip.departureTime ?: "-"}")
                    Text("Route: ${trip.route.name}")
                } else {
                    Text("No active trip.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}


// Shows quick actions for the user's role (parent, admin, driver)
@Composable
fun MySpaceSection(user: User, navController: NavController, trips: List<Trip>) {
    val userRole = UserRole.fromString(user.role)
    when (userRole) {
        is UserRole.Parent -> {
            ParentSpaceSection(navController = navController)
        }
        is UserRole.Admin -> {
            AdminSpaceSection(navController = navController)
        }
        is UserRole.Driver -> {
            DriverSpaceSection(navController = navController, trips = trips)
        }
    }
}


// Quick actions for parents
@Composable
fun ParentSpaceSection(navController: NavController) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        MySpaceCard(
            title = "Add Student",
            icon = Icons.Default.PersonAdd,
            onClick = { navController.navigate("add_student") }
        )
        MySpaceCard(
            title = "View My Students",
            icon = Icons.Default.People,
            onClick = { navController.navigate("view_my_students") }
        )
        MySpaceCard(
            title = "View Student Trips",
            icon = Icons.Default.DirectionsBus,
            onClick = { navController.navigate("trips_list") }
        )
        MySpaceCard(
            title = "Download Trip Report",
            icon = Icons.Default.Download,
            onClick = { navController.navigate("trip_reports") }
        )
    }
}


// Quick actions for admins
@Composable
fun AdminSpaceSection(navController: NavController) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        MySpaceCard(
            title = "Manage Routes",
            icon = Icons.Default.Map,
            onClick = { navController.navigate("manage_routes") }
        )
        MySpaceCard(
            title = "Manage Buses",
            icon = Icons.Default.DirectionsBus,
            onClick = { navController.navigate("manage_buses") }
        )
        MySpaceCard(
            title = "Students",
            icon = Icons.Default.People,
            onClick = { navController.navigate("manage_students") }
        )
        MySpaceCard(
            title = "Manage Drivers",
            icon = Icons.Default.Person,
            onClick = { navController.navigate("manage_drivers") }
        )
        MySpaceCard(
            title = "Manage Trips",
            icon = Icons.Default.CalendarToday,
            onClick = { navController.navigate("manage_trips") }
        )
    }
}


// Quick actions for drivers
@Composable
fun DriverSpaceSection(navController: NavController, trips: List<Trip>) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        MySpaceCard(
            title = "My Trip",
            icon = Icons.Default.DirectionsBus,
            onClick = { navController.navigate("driver_my_trip") }
        )
        MySpaceCard(
            title = "Download Trip Report",
            icon = Icons.Default.Download,
            onClick = { navController.navigate("trip_reports") }
        )
    }
}


// Card for a single quick action in MySpaceSection
@Composable
fun MySpaceCard(title: String, icon: androidx.compose.ui.graphics.vector.ImageVector, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, contentDescription = title, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(24.dp))
            Spacer(modifier = Modifier.width(16.dp))
            Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Medium)
        }
    }
}


// Card for showing bus status (currently static content)
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


// Card showing a summary of upcoming trips
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
                        Text("${trip.departureTime ?: "-"} | ${trip.route.name}", fontSize = 14.sp)
                    }
                }
            }
        }
    }
}


// Card for a dashboard action (e.g., live tracking, notifications)
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


// Bottom navigation bar for dashboard
@Composable
fun BottomNavigationBar(navController: NavController, user: User) {
    NavigationBar {
        NavigationBarItem(
            icon = { Icon(Icons.Filled.Home, contentDescription = "Home") },
            label = { Text("Home") },
            selected = true, // Always selected on the dashboard
            onClick = { /* No-op, already home */ }
        )
        if (UserRole.fromString(user.role) is UserRole.Admin) {
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


// Section showing personalized trip schedule for the user
@Composable
fun PersonalizedTripsSection(user: User, trips: List<Trip>) {
    val filteredTrips = when (UserRole.fromString(user.role)) {
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


// Banner for showing in-app notifications (not always used)
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


// Utility for showing heads-up notifications (used for important alerts)
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
