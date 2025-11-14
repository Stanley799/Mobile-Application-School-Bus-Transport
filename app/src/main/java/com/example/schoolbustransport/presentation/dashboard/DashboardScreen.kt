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
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
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
import com.example.schoolbustransport.ui.theme.SchoolBusTransportTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    user: User,
    authViewModel: AuthViewModel = hiltViewModel(),
    navController: NavController
) {
    SchoolBusTransportTheme {
        Scaffold(
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
                    UpcomingTripsCard(modifier = Modifier.weight(1f))
                    BusStopsCard(modifier = Modifier.weight(1f))
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // Bottom action cards
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    ActionCard(title = "Book a Ride", subtitle = "Contact Support", modifier = Modifier.weight(1f))
                    ActionCard(title = "Notifications", subtitle = "Contact Support", modifier = Modifier.weight(1f), isPrimary = false)
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
fun UpcomingTripsCard(modifier: Modifier = Modifier) {
    Card(modifier = modifier.height(150.dp), shape = RoundedCornerShape(20.dp)) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Upcoming Trips", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(16.dp))
            // Placeholder for timeline UI
            Text("2:40pm -> 11:9pm")
            Text("0:30pm -> 051pm")
        }
    }
}

@Composable
fun BusStopsCard(modifier: Modifier = Modifier) {
    Card(modifier = modifier.height(150.dp), shape = RoundedCornerShape(20.dp)) {
        Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text("Bus Stops", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))
            Box(
                modifier = Modifier
                    .size(60.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
            ) // Placeholder for mini-map
            Text("17.24+ Estimated Arrival", fontSize = 12.sp, modifier = Modifier.padding(top = 8.dp))
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
