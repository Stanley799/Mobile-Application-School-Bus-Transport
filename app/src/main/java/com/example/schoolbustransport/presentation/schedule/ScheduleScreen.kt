package com.example.schoolbustransport.presentation.schedule

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScheduleScreen(navController: NavController) {

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Admin Schedule Management") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
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
            Text("Trip Management", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            AdminActionCard(
                title = "Manage Trips",
                icon = Icons.Default.DateRange,
                onClick = { /* TODO: navController.navigate("manage_trips") */ }
            )
            
            Text("Asset Management", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            AdminActionCard(
                title = "Manage Buses",
                icon = Icons.Default.DirectionsBus,
                onClick = { /* TODO: navController.navigate("manage_buses") */ }
            )
            AdminActionCard(
                title = "Manage Routes",
                icon = Icons.Default.Map,
                onClick = { /* TODO: navController.navigate("manage_routes") */ }
            )

            Text("Personnel Management", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            AdminActionCard(
                title = "Manage Drivers",
                icon = Icons.Default.Person,
                onClick = { /* TODO: navController.navigate("manage_drivers") */ }
            )
        }
    }
}

@Composable
fun AdminActionCard(title: String, icon: androidx.compose.ui.graphics.vector.ImageVector, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(4.dp),
        onClick = onClick
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Icon(imageVector = icon, contentDescription = null, modifier = Modifier.padding(bottom = 8.dp))
            Text(title, style = MaterialTheme.typography.titleLarge)
        }
    }
}
