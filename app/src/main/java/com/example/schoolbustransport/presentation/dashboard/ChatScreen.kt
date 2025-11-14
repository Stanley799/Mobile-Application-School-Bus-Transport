package com.example.schoolbustransport.presentation.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(navController: NavController, conversationId: String?, vm: MessagesViewModel = hiltViewModel()) {
	var messageText by remember { mutableStateOf("") }
	// Convert route argument to an Int user id for the backend
	val otherId = conversationId?.toIntOrNull()

	LaunchedEffect(otherId) {
		otherId?.let { vm.loadMessages(it) }
	}

	val messages by vm.messages.collectAsState()
	val loading by vm.isLoading.collectAsState()
	val error by vm.error.collectAsState()
	val myUserId by vm.myUserId.collectAsState()

	Scaffold(
		topBar = {
			TopAppBar(
				title = { Text("Chat") },
				navigationIcon = {
					IconButton(onClick = { navController.popBackStack() }) {
						Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
					}
				}
			)
		},
		bottomBar = {
			MessageInput(onSendMessage = {
				if (!it.isNullOrBlank() && otherId != null) {
					vm.sendMessage(otherId, it) { messageText = "" }
				}
			})
		}
	) { paddingValues ->
		Column(modifier = Modifier.padding(paddingValues).fillMaxSize()) {
			if (loading) LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
			if (!error.isNullOrBlank()) Text(error!!, color = MaterialTheme.colorScheme.error)
			LazyColumn(
				modifier = Modifier.weight(1f).fillMaxWidth(),
				contentPadding = PaddingValues(8.dp),
				verticalArrangement = Arrangement.spacedBy(4.dp),
				reverseLayout = true
			) {
				items(messages.reversed()) { m ->
					val isFromMe = m.senderId == myUserId
					val avatarColor = if (isFromMe) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary
					Row(
						modifier = Modifier.fillMaxWidth(),
						horizontalArrangement = if (isFromMe) Arrangement.End else Arrangement.Start
					) {
						if (!isFromMe) {
							AvatarBubble(m.sender?.name ?: "", avatarColor)
						}
						Column(horizontalAlignment = if (isFromMe) Alignment.End else Alignment.Start) {
							MessageBubble(
								text = m.content,
								isFromMe = isFromMe,
								timestamp = m.timestamp,
								senderName = m.sender?.name ?: "Me"
							)
						}
						if (isFromMe) {
							AvatarBubble("Me", avatarColor)
						}
					}
				}
			}
		}
	}
}

@Composable
private fun MessageBubble(text: String, isFromMe: Boolean, timestamp: String, senderName: String) {
	val backgroundColor = if (isFromMe) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant
	val textColor = if (isFromMe) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
	Column(
		modifier = Modifier
			.widthIn(max = 320.dp)
			.clip(RoundedCornerShape(16.dp))
			.background(backgroundColor)
			.padding(12.dp)
	) {
		if (!isFromMe) Text(senderName, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.secondary)
		Text(text, color = textColor, style = MaterialTheme.typography.bodyLarge)
		Spacer(modifier = Modifier.height(4.dp))
		Text(timestamp, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline, modifier = Modifier.align(Alignment.End))
	}
}

@Composable
private fun AvatarBubble(name: String, color: Color) {
	Box(
		modifier = Modifier
			.size(32.dp)
			.clip(RoundedCornerShape(50))
			.background(color),
		contentAlignment = Alignment.Center
	) {
		Text(
			text = name.take(1).uppercase(),
			color = MaterialTheme.colorScheme.onPrimary,
			style = MaterialTheme.typography.titleMedium
		)
	}
}

@Composable
private fun MessageInput(onSendMessage: (String) -> Unit) {
	var text by remember { mutableStateOf("") }

	Card(modifier = Modifier.fillMaxWidth().padding(8.dp), elevation = CardDefaults.cardElevation(8.dp)) {
		Row(
			modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
			verticalAlignment = Alignment.CenterVertically
		) {
			TextField(
				value = text,
				onValueChange = { text = it },
				modifier = Modifier.weight(1f),
				placeholder = { Text("Type a message...") },
				colors = TextFieldDefaults.colors(focusedIndicatorColor = Color.Transparent, unfocusedIndicatorColor = Color.Transparent)
			)
			IconButton(onClick = {
				if (text.isNotBlank()) {
					onSendMessage(text)
					text = ""
				}
			}) {
				Icon(Icons.Default.Send, contentDescription = "Send Message")
			}
		}
	}
}
