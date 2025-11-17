package com.example.schoolbustransport.presentation.schedule

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.schoolbustransport.domain.model.Route

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManageRoutesScreen(navController: NavController, viewModel: ManageRoutesViewModel = hiltViewModel()) {
    val routes by viewModel.routes.collectAsState()
    val loading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    
    var showAddDialog by remember { mutableStateOf(false) }
    var routeName by remember { mutableStateOf("") }
    var routeFrom by remember { mutableStateOf("") }
    var routeTo by remember { mutableStateOf("") }
    var routeDistance by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        viewModel.loadRoutes()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Manage Routes") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { showAddDialog = true }) {
                        Icon(Icons.Default.Add, contentDescription = "Add Route")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (error != null) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
                ) {
                    Text(
                        "Error: $error",
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }

            if (loading && routes.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else if (routes.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("No routes created yet", style = MaterialTheme.typography.titleMedium)
                        Spacer(Modifier.height(8.dp))
                        Text("Tap the + button to add a route", style = MaterialTheme.typography.bodyMedium)
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(routes) { route ->
                        RouteCard(route = route)
                    }
                }
            }
        }
    }

    // Add Route Dialog
    if (showAddDialog) {
        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = { Text("Add New Route") },
            text = {
                Column(
                    modifier = Modifier.verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedTextField(
                        value = routeName,
                        onValueChange = { routeName = it },
                        label = { Text("Route Name *") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = routeFrom,
                        onValueChange = { routeFrom = it },
                        label = { Text("From *") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = routeTo,
                        onValueChange = { routeTo = it },
                        label = { Text("To *") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = routeDistance,
                        onValueChange = { routeDistance = it },
                        label = { Text("Distance Covered *") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        placeholder = { Text("e.g., 15 km") }
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (routeName.isNotBlank() && routeFrom.isNotBlank() && 
                            routeTo.isNotBlank() && routeDistance.isNotBlank()) {
                            viewModel.createRoute(routeName, routeFrom, routeTo, routeDistance)
                            routeName = ""
                            routeFrom = ""
                            routeTo = ""
                            routeDistance = ""
                            showAddDialog = false
                        }
                    },
                    enabled = routeName.isNotBlank() && routeFrom.isNotBlank() && 
                        routeTo.isNotBlank() && routeDistance.isNotBlank() && !loading
                ) {
                    Text("Add Route")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun RouteCard(route: Route) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                route.name,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(8.dp))
            Text("From: ${route.from}", style = MaterialTheme.typography.bodyMedium)
            Text("To: ${route.to}", style = MaterialTheme.typography.bodyMedium)
            Text("Distance: ${route.distance}", style = MaterialTheme.typography.bodyMedium)
        }
    }
}
