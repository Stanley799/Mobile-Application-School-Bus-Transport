package com.example.schoolbustransport.presentation.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.schoolbustransport.domain.model.UserRole
import com.example.schoolbustransport.presentation.auth.AuthViewModel
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    navController: NavController, 
    conversationId: String?, 
    vm: MessagesViewModel = hiltViewModel(),
    authViewModel: AuthViewModel = hiltViewModel()
) {
    val otherId = conversationId?.toIntOrNull()
    val user by authViewModel.loginState.collectAsState()

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
            MessageInput(userRole = (user as? com.example.schoolbustransport.presentation.auth.LoginState.Success)?.user?.role, onSendMessage = {
                text, isNotification ->
                if (otherId != null) {
                    vm.sendMessage(otherId, text, if (isNotification) "notification" else "chat") {}
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
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
                        horizontalArrangement = if (isFromMe) Arrangement.End else Arrangement.Start,
                        verticalAlignment = Alignment.Bottom
                    ) {
                        if (!isFromMe) {
                            AvatarBubble(m.sender?.name ?: "", MaterialTheme.colorScheme.secondary)
                            Spacer(modifier = Modifier.width(6.dp))
                        }
                        MessageBubble(
                            text = m.content,
                            isFromMe = isFromMe,
                            timestamp = m.timestamp,
                            senderName = m.sender?.name ?: "Me",
                            isLast = m == messages.lastOrNull(),
                            status = if (isFromMe) "Sent" else "Received"
                        )
                        if (isFromMe) {
                            Spacer(modifier = Modifier.width(6.dp))
                            AvatarBubble("Me", MaterialTheme.colorScheme.primary)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MessageBubble(
	text: String,
	isFromMe: Boolean,
	timestamp: String,
	senderName: String,
	isLast: Boolean = false,
	status: String = ""
) {
	val backgroundColor = if (isFromMe) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant
	val textColor = if (isFromMe) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
	val time = try {
		OffsetDateTime.parse(timestamp).format(DateTimeFormatter.ofPattern("HH:mm"))
	} catch (_: Exception) { timestamp }

	Column(
		modifier = Modifier
			.widthIn(max = 320.dp)
			.clip(RoundedCornerShape(18.dp))
			.background(backgroundColor)
			.padding(horizontal = 14.dp, vertical = 10.dp)
	) {
		if (!isFromMe) {
			Text(
				text = senderName,
				style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
				color = MaterialTheme.colorScheme.primary,
				modifier = Modifier.padding(bottom = 2.dp)
			)
		}
		Text(
			text,
			style = MaterialTheme.typography.bodyLarge,
			color = textColor,
			modifier = Modifier.padding(bottom = 2.dp)
		)
		Row(
			modifier = Modifier.fillMaxWidth(),
			horizontalArrangement = Arrangement.SpaceBetween,
			verticalAlignment = Alignment.CenterVertically
		) {
			Text(
				text = time,
				style = MaterialTheme.typography.labelSmall,
				color = textColor.copy(alpha = 0.7f),
				textAlign = TextAlign.Start
			)
			if (isFromMe && isLast) {
				Text(
					text = status,
					style = MaterialTheme.typography.labelSmall,
					color = MaterialTheme.colorScheme.secondary,
					textAlign = TextAlign.End,
					modifier = Modifier.padding(start = 8.dp)
				)
			}
		}
	}
}

@Composable
private fun AvatarBubble(name: String, color: Color) {
	Box(
		modifier = Modifier
			.size(32.dp)
			.clip(CircleShape) // Using CircleShape for a perfectly round avatar
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MessageInput(userRole: UserRole?, onSendMessage: (String, Boolean) -> Unit) {
	var text by remember { mutableStateOf("") }
    var isNotification by remember { mutableStateOf(false) }

	Card(modifier = Modifier.fillMaxWidth().padding(8.dp), elevation = CardDefaults.cardElevation(4.dp)) {
        Column {
            if (userRole is UserRole.Admin || userRole is UserRole.Driver) {
                Row(modifier = Modifier.padding(start = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                    Switch(checked = isNotification, onCheckedChange = { isNotification = it })
                    Spacer(Modifier.width(8.dp))
                    Text("Send as Notification")
                }
            }
            Row(
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextField(
                    value = text,
                    onValueChange = { text = it },
                    modifier = Modifier.weight(1f),
                    placeholder = { Text("Type a message...") },
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        disabledContainerColor = Color.Transparent,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent
                    )
                )
                IconButton(onClick = {
                    if (text.isNotBlank()) {
                        onSendMessage(text, isNotification)
                        text = ""
                        isNotification = false
                    }
                }) {
                    Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Send Message")
                }
            }
        }
	}
}
