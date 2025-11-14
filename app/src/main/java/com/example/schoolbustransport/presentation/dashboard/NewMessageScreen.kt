package com.example.schoolbustransport.presentation.dashboard

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewMessageScreen(navController: NavController, vm: MessagesViewModel = hiltViewModel()) {
    var search by remember { mutableStateOf(TextFieldValue("")) }
    val users by vm.availableRecipients.collectAsState()
    val loading by vm.isLoading.collectAsState()
    val error by vm.error.collectAsState()

    LaunchedEffect(Unit) { vm.loadAvailableRecipients() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("New Message") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(modifier = Modifier.padding(paddingValues).fillMaxSize()) {
            OutlinedTextField(
                value = search,
                onValueChange = { search = it; vm.filterRecipients(it.text) },
                label = { Text("Search users") },
                modifier = Modifier.fillMaxWidth().padding(8.dp)
            )
            if (loading) LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            if (!error.isNullOrBlank()) Text(error!!, color = MaterialTheme.colorScheme.error)
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                items(users) { user ->
                    ListItem(
                        headlineContent = { Text(user.name) },
                        supportingContent = { Text(user.role) },
                        modifier = Modifier.clickable {
                            navController.navigate("chat/${user.id}")
                        }
                    )
                    Divider()
                }
            }
        }
    }
}
