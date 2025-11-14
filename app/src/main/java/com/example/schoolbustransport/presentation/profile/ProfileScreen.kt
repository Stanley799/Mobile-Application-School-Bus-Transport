package com.example.schoolbustransport.presentation.profile

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.schoolbustransport.domain.model.User

@Composable
fun ProfileScreen(user: User) {
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Profile") }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = user.name, style = MaterialTheme.typography.headlineMedium)
            Text(text = user.role, style = MaterialTheme.typography.titleMedium)
            Text(text = user.phone ?: "No phone", style = MaterialTheme.typography.bodyLarge)
            // Add more profile fields/settings here
            Button(onClick = { /* TODO: Implement logout or edit profile */ }) {
                Text("Edit Profile")
            }
        }
    }
}
