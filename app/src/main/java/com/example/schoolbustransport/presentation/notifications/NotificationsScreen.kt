package com.example.schoolbustransport.presentation.notifications

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun NotificationsScreen(notifications: List<String> = emptyList()) {
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Notifications") }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (notifications.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No notifications yet.", style = MaterialTheme.typography.bodyLarge)
                }
            } else {
                notifications.forEach { notif ->
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Text(notif, modifier = Modifier.padding(16.dp))
                    }
                }
            }
        }
    }
}
