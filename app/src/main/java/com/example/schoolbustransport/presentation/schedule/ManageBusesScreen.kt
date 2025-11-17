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
import com.example.schoolbustransport.domain.model.Bus

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManageBusesScreen(navController: NavController, viewModel: ManageBusesViewModel = hiltViewModel()) {
    val buses by viewModel.buses.collectAsState()
    val loading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    
    var showAddDialog by remember { mutableStateOf(false) }
    var busName by remember { mutableStateOf("") }
    var numberOfSeats by remember { mutableStateOf("") }
    var numberPlate by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        viewModel.loadBuses()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Manage Buses") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { showAddDialog = true }) {
                        Icon(Icons.Default.Add, contentDescription = "Add Bus")
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

            if (loading && buses.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else if (buses.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("No buses registered yet", style = MaterialTheme.typography.titleMedium)
                        Spacer(Modifier.height(8.dp))
                        Text("Tap the + button to add a bus", style = MaterialTheme.typography.bodyMedium)
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(buses) { bus ->
                        BusCard(bus = bus)
                    }
                }
            }
        }
    }

    // Add Bus Dialog
    if (showAddDialog) {
        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = { Text("Add New Bus") },
            text = {
                Column(
                    modifier = Modifier.verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedTextField(
                        value = busName,
                        onValueChange = { busName = it },
                        label = { Text("Bus Name *") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = numberPlate,
                        onValueChange = { numberPlate = it },
                        label = { Text("Number Plate *") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = numberOfSeats,
                        onValueChange = { if (it.all { char -> char.isDigit() }) numberOfSeats = it },
                        label = { Text("Number of Seats *") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (busName.isNotBlank() && numberPlate.isNotBlank() && numberOfSeats.isNotBlank()) {
                            viewModel.createBus(busName, numberPlate, numberOfSeats.toIntOrNull() ?: 0)
                            busName = ""
                            numberPlate = ""
                            numberOfSeats = ""
                            showAddDialog = false
                        }
                    },
                    enabled = busName.isNotBlank() && numberPlate.isNotBlank() && 
                        numberOfSeats.isNotBlank() && !loading
                ) {
                    Text("Add Bus")
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
fun BusCard(bus: Bus) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                bus.name.ifBlank { bus.licensePlate },
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(8.dp))
            Text("Number Plate: ${bus.licensePlate}", style = MaterialTheme.typography.bodyMedium)
            Text("Seats: ${bus.numberOfSeats}", style = MaterialTheme.typography.bodyMedium)
        }
    }
}
