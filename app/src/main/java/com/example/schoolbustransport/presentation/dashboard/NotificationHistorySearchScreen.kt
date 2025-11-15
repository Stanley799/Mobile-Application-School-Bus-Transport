package com.example.schoolbustransport.presentation.dashboard

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel

@Composable
fun NotificationHistorySearchScreen(vm: MessagesViewModel = hiltViewModel()) {
    val conversations by vm.conversations.collectAsState()
    val loading by vm.isLoading.collectAsState()
    val error by vm.error.collectAsState()
    var searchQuery by remember { mutableStateOf(TextFieldValue()) }

    val filteredConversations = conversations.filter {
        it.userName.contains(searchQuery.text, ignoreCase = true) ||
        it.lastMessage.contains(searchQuery.text, ignoreCase = true)
    }

    LaunchedEffect(Unit) { vm.loadConversations() }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            "Notification & Message History",
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search messages") },
            label = { Text("Search by name or message") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )
        Spacer(modifier = Modifier.height(16.dp))
        if (loading) {
            LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
        } else if (!error.isNullOrBlank()) {
            Text(error!!, color = MaterialTheme.colorScheme.error)
        } else if (filteredConversations.isEmpty()) {
            Spacer(modifier = Modifier.height(48.dp))
            // Illustration (replace with your own drawable if available)
            // Image(painter = painterResource(id = R.drawable.ic_empty_state), contentDescription = null, modifier = Modifier.size(120.dp))
            Icon(Icons.Default.Notifications, contentDescription = null, modifier = Modifier.size(80.dp), tint = Color.LightGray)
            Spacer(modifier = Modifier.height(16.dp))
            Text("No notifications or messages found.", color = Color.Gray, style = MaterialTheme.typography.bodyLarge)
            Text("Try a different search or check back later.", color = Color.Gray, style = MaterialTheme.typography.bodyMedium)
        } else {
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                items(filteredConversations) { convo ->
                    NotificationHistoryItem(
                        name = convo.userName,
                        lastMessage = convo.lastMessage,
                        timestamp = convo.lastMessageTime
                    )
                }
            }
        }
    }
}
