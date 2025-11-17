package com.example.schoolbustransport.presentation.schedule

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.schoolbustransport.domain.model.Bus
import com.example.schoolbustransport.domain.model.Route
import com.example.schoolbustransport.domain.model.User
import java.util.Date

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManageTripsScreen(navController: NavController, viewModel: ManageTripsViewModel = hiltViewModel()) {
    val buses by viewModel.buses.collectAsState()
    val routes by viewModel.routes.collectAsState()
    val drivers by viewModel.drivers.collectAsState()
    val loading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()

    var tripName by remember { mutableStateOf("") }
    var selectedRoute by remember { mutableStateOf<Route?>(null) }
    var selectedBus by remember { mutableStateOf<Bus?>(null) }
    var selectedGrade by remember { mutableStateOf("") }
    var selectedDriver by remember { mutableStateOf<User?>(null) }
    var departureDate by remember { mutableStateOf("") }
    var departureTime by remember { mutableStateOf("") }

    val grades = listOf("Grade5", "Grade6", "Grade7", "Grade8", "Grade9")

    LaunchedEffect(Unit) {
        viewModel.loadLists()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Create Trip") },
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
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            if (error != null) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
                ) {
                    Text(
                        "Error: $error",
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        modifier = Modifier.padding(16.dp)
                    )
                }
                Spacer(Modifier.height(16.dp))
            }

            OutlinedTextField(
                value = tripName,
                onValueChange = { tripName = it },
                label = { Text("Trip Name *") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            Spacer(Modifier.height(16.dp))

            // Route Selection
            var routeExpanded by remember { mutableStateOf(false) }
            ExposedDropdownMenuBox(
                expanded = routeExpanded,
                onExpandedChange = { routeExpanded = !routeExpanded }
            ) {
                OutlinedTextField(
                    value = selectedRoute?.name ?: "",
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Select Route *") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = routeExpanded) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor()
                )
                ExposedDropdownMenu(
                    expanded = routeExpanded,
                    onDismissRequest = { routeExpanded = false }
                ) {
                    routes.forEach { route ->
                        DropdownMenuItem(
                            text = { Text("${route.name} (${route.from} - ${route.to})") },
                            onClick = {
                                selectedRoute = route
                                routeExpanded = false
                            }
                        )
                    }
                }
            }
            Spacer(Modifier.height(16.dp))

            // Bus Selection
            var busExpanded by remember { mutableStateOf(false) }
            ExposedDropdownMenuBox(
                expanded = busExpanded,
                onExpandedChange = { busExpanded = !busExpanded }
            ) {
                OutlinedTextField(
                    value = selectedBus?.name?.ifBlank { selectedBus?.licensePlate } ?: "",
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Select Bus *") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = busExpanded) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor()
                )
                ExposedDropdownMenu(
                    expanded = busExpanded,
                    onDismissRequest = { busExpanded = false }
                ) {
                    buses.forEach { bus ->
                        DropdownMenuItem(
                            text = { Text("${bus.name.ifBlank { bus.licensePlate }} (${bus.numberOfSeats} seats)") },
                            onClick = {
                                selectedBus = bus
                                busExpanded = false
                            }
                        )
                    }
                }
            }
            Spacer(Modifier.height(16.dp))

            // Grade Selection
            var gradeExpanded by remember { mutableStateOf(false) }
            ExposedDropdownMenuBox(
                expanded = gradeExpanded,
                onExpandedChange = { gradeExpanded = !gradeExpanded }
            ) {
                OutlinedTextField(
                    value = selectedGrade,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Select Student Class (Grade) *") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = gradeExpanded) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor()
                )
                ExposedDropdownMenu(
                    expanded = gradeExpanded,
                    onDismissRequest = { gradeExpanded = false }
                ) {
                    grades.forEach { grade ->
                        DropdownMenuItem(
                            text = { Text(grade) },
                            onClick = {
                                selectedGrade = grade
                                gradeExpanded = false
                            }
                        )
                    }
                }
            }
            Spacer(Modifier.height(16.dp))

            // Driver Selection
            var driverExpanded by remember { mutableStateOf(false) }
            ExposedDropdownMenuBox(
                expanded = driverExpanded,
                onExpandedChange = { driverExpanded = !driverExpanded }
            ) {
                OutlinedTextField(
                    value = selectedDriver?.name ?: "",
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Select Driver *") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = driverExpanded) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor()
                )
                ExposedDropdownMenu(
                    expanded = driverExpanded,
                    onDismissRequest = { driverExpanded = false }
                ) {
                    drivers.forEach { driver ->
                        DropdownMenuItem(
                            text = { Text("${driver.name} (${driver.phone ?: "No phone"})") },
                            onClick = {
                                selectedDriver = driver
                                driverExpanded = false
                            }
                        )
                    }
                }
            }
            Spacer(Modifier.height(16.dp))

            OutlinedTextField(
                value = departureDate,
                onValueChange = { departureDate = it },
                label = { Text("Departure Date *") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                placeholder = { Text("YYYY-MM-DD") }
            )
            Spacer(Modifier.height(16.dp))

            OutlinedTextField(
                value = departureTime,
                onValueChange = { departureTime = it },
                label = { Text("Departure Time *") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                placeholder = { Text("HH:MM") }
            )
            Spacer(Modifier.height(24.dp))

            Button(
                onClick = {
                    if (tripName.isNotBlank() && selectedRoute != null && selectedBus != null &&
                        selectedGrade.isNotBlank() && selectedDriver != null &&
                        departureDate.isNotBlank() && departureTime.isNotBlank()) {
                        viewModel.createTrip(
                            tripName = tripName,
                            route = selectedRoute!!,
                            bus = selectedBus!!,
                            grade = selectedGrade,
                            driver = selectedDriver!!,
                            departureDate = departureDate,
                            departureTime = departureTime
                        )
                        // Clear form
                        tripName = ""
                        selectedRoute = null
                        selectedBus = null
                        selectedGrade = ""
                        selectedDriver = null
                        departureDate = ""
                        departureTime = ""
                    }
                },
                enabled = !loading && tripName.isNotBlank() && selectedRoute != null && 
                    selectedBus != null && selectedGrade.isNotBlank() && selectedDriver != null &&
                    departureDate.isNotBlank() && departureTime.isNotBlank(),
                modifier = Modifier.fillMaxWidth()
            ) {
                if (loading) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp))
                } else {
                    Text("Create Trip")
                }
            }
        }
    }
}
