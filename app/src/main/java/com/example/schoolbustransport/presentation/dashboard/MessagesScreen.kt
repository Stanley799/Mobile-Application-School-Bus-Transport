package com.example.schoolbustransport.presentation.dashboard

import androidx.compose.foundation.background
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.Date
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MessagesScreen(navController: NavController, vm: MessagesViewModel = hiltViewModel()) {
	LaunchedEffect(Unit) { vm.loadConversations() }
	val conversations by vm.conversations.collectAsState()
	val loading by vm.isLoading.collectAsState()
	val error by vm.error.collectAsState()
	val myUserId by vm.myUserId.collectAsState()

	Scaffold(
		topBar = {
			TopAppBar(
				title = { Text("Messages") },
				navigationIcon = {
					IconButton(onClick = { navController.popBackStack() }) {
						Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
					}
				}
			)
		},
		floatingActionButton = {
			FloatingActionButton(onClick = { navController.navigate("new_message") }) {
				Icon(Icons.Default.Add, contentDescription = "New Message")
			}
		}
	) { paddingValues ->
		Column(modifier = Modifier.padding(paddingValues).fillMaxSize()) {
			if (loading) {
				LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
			} else if (!error.isNullOrBlank()) {
				Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
					Text(error!!, color = MaterialTheme.colorScheme.error)
				}
			} else if (conversations.isEmpty()) {
				Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
					Column(horizontalAlignment = Alignment.CenterHorizontally) {
						Text("No conversations found.", style = MaterialTheme.typography.bodyLarge)
						Spacer(Modifier.height(16.dp))
						Button(onClick = { navController.navigate("new_message") }) {
							Text("Start New Conversation")
						}
						Spacer(Modifier.height(8.dp))
						Text("Or tap the + button below.", style = MaterialTheme.typography.bodySmall)
					}
				}
			} else {
				LazyColumn(
					modifier = Modifier.fillMaxSize(),
					contentPadding = PaddingValues(vertical = 8.dp)
				) {
					val filteredConversations = conversations.filter { 
						!it.userId.isNullOrBlank() && 
						it.userId != myUserId && 
						!it.userName.isNullOrBlank() &&
						it.userName != "Unknown"
					}
					if (filteredConversations.isEmpty()) {
						item {
							Box(modifier = Modifier.fillMaxSize().padding(16.dp), contentAlignment = Alignment.Center) {
								Text("No conversations found.", style = MaterialTheme.typography.bodyLarge)
							}
						}
					} else {
						items(filteredConversations) { convo ->
							ConversationItem(
								name = convo.userName ?: "Unknown",
								lastMessage = convo.lastMessage ?: "",
								timestamp = convo.lastMessageTime ?: "",
								onClick = {
									val userId = convo.userId
									if (!userId.isNullOrBlank() && userId != myUserId) {
										try {
											navController.navigate("chat/$userId") {
												// Prevent multiple instances
												launchSingleTop = true
											}
										} catch (e: Exception) {
											// Log error for debugging
											android.util.Log.e("MessagesScreen", "Navigation error", e)
										}
									}
								}
							)
						}
					}
				}
			}
		}
	}
}

@Composable
private fun ConversationItem(name: String, lastMessage: String, timestamp: String, onClick: () -> Unit, unreadCount: Int = 0) {
	Row(
		modifier = Modifier
			.fillMaxWidth()
			.clickable(onClick = onClick)
			.padding(horizontal = 12.dp, vertical = 10.dp),
		verticalAlignment = Alignment.CenterVertically
	) {
		Box(
			modifier = Modifier
				.size(44.dp)
				.clip(CircleShape)
				.background(MaterialTheme.colorScheme.primaryContainer),
			contentAlignment = Alignment.Center
		) {
			Text(
				text = name.take(1).uppercase(),
				style = MaterialTheme.typography.titleLarge,
				color = MaterialTheme.colorScheme.onPrimaryContainer
			)
		}
		Spacer(modifier = Modifier.width(12.dp))
		Column(modifier = Modifier.weight(1f)) {
			Row(verticalAlignment = Alignment.CenterVertically) {
				Text(name, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
				if (unreadCount > 0) {
					Spacer(modifier = Modifier.width(6.dp))
					Badge(containerColor = MaterialTheme.colorScheme.error) {
						Text(unreadCount.toString(), color = MaterialTheme.colorScheme.onError)
					}
				}
			}
			Text(lastMessage, style = MaterialTheme.typography.bodyMedium, maxLines = 1, color = MaterialTheme.colorScheme.onSurfaceVariant)
		}
		Spacer(modifier = Modifier.width(12.dp))
		val time = try {
			// Try to parse ISO 8601 or fallback to raw string
			val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
			val outputFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
			val date: Date? = inputFormat.parse(timestamp)
			if (date != null) outputFormat.format(date) else timestamp
		} catch (_: Exception) { timestamp }
		Text(time, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
	}
}
