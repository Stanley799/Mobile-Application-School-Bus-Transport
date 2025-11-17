package com.example.schoolbustransport.presentation.dashboard

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.schoolbustransport.domain.model.Student
import com.example.schoolbustransport.domain.model.Trip
import com.example.schoolbustransport.domain.model.TripStatus
import com.example.schoolbustransport.presentation.trip.TripViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.util.Date

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DriverMyTripScreen(navController: NavController) {
    val tripViewModel: TripViewModel = hiltViewModel()
    val tripState by tripViewModel.tripState.collectAsState()
    val auth = remember { FirebaseAuth.getInstance() }
    val firestore = remember { FirebaseFirestore.getInstance() }
    
    var selectedTrip by remember { mutableStateOf<Trip?>(null) }
    var attendanceMap by remember { mutableStateOf<Map<String, Boolean>>(emptyMap()) }
    var tripStarted by remember { mutableStateOf(false) }
    var startTime by remember { mutableStateOf<Date?>(null) }
    var endTime by remember { mutableStateOf<Date?>(null) }

    LaunchedEffect(Unit) {
        tripViewModel.loadTrips()
    }

    // Filter trips assigned to current driver
    val driverTrips = when (val state = tripState) {
        is com.example.schoolbustransport.presentation.trip.TripState.Success -> {
            state.trips.filter { it.driver.id == auth.currentUser?.uid }
        }
        else -> emptyList()
    }

    // Auto-select first trip if available
    LaunchedEffect(driverTrips) {
        if (selectedTrip == null && driverTrips.isNotEmpty()) {
            selectedTrip = driverTrips.first()
            // Load attendance for selected trip
            selectedTrip?.students?.forEach { student ->
                attendanceMap = attendanceMap + (student.id to false)
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("My Trip") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        when (val state = tripState) {
            is com.example.schoolbustransport.presentation.trip.TripState.Loading -> {
                Box(modifier = Modifier.fillMaxSize().padding(paddingValues), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
            is com.example.schoolbustransport.presentation.trip.TripState.Error -> {
                Box(modifier = Modifier.fillMaxSize().padding(paddingValues), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Error loading trips", color = MaterialTheme.colorScheme.error)
                        Text(state.message)
                    }
                }
            }
            is com.example.schoolbustransport.presentation.trip.TripState.Success -> {
                if (driverTrips.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize().padding(paddingValues), contentAlignment = Alignment.Center) {
                        Text("No trips assigned to you", style = MaterialTheme.typography.titleMedium)
                    }
                } else {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(paddingValues)
                            .padding(16.dp)
                    ) {
                        // Trip Selection
                        if (driverTrips.size > 1) {
                            Text("Select Trip:", style = MaterialTheme.typography.titleMedium)
                            Spacer(Modifier.height(8.dp))
                            driverTrips.forEach { trip ->
                                FilterChip(
                                    selected = selectedTrip?.id == trip.id,
                                    onClick = {
                                        selectedTrip = trip
                                        attendanceMap = trip.students.associate { it.id to false }
                                    },
                                    label = { Text(trip.tripName.ifBlank { trip.id }) }
                                )
                                Spacer(Modifier.height(4.dp))
                            }
                            Spacer(Modifier.height(16.dp))
                        }

                        selectedTrip?.let { trip ->
                            // Trip Details
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Text(
                                        trip.tripName.ifBlank { trip.id },
                                        style = MaterialTheme.typography.titleLarge,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Spacer(Modifier.height(8.dp))
                                    Text("Route: ${trip.route.name}", style = MaterialTheme.typography.bodyMedium)
                                    Text("Bus: ${trip.bus.licensePlate}", style = MaterialTheme.typography.bodyMedium)
                                    Text("Status: ${trip.status}", style = MaterialTheme.typography.bodyMedium)
                                    if (startTime != null) {
                                        Text("Started: ${startTime}", style = MaterialTheme.typography.bodySmall)
                                    }
                                    if (endTime != null) {
                                        Text("Ended: ${endTime}", style = MaterialTheme.typography.bodySmall)
                                    }
                                }
                            }
                            Spacer(Modifier.height(16.dp))

                            // Student List with Attendance
                            Text("Students:", style = MaterialTheme.typography.titleMedium)
                            Spacer(Modifier.height(8.dp))
                            LazyColumn(
                                modifier = Modifier.weight(1f),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                items(trip.students) { student ->
                                    StudentAttendanceRow(
                                        student = student,
                                        isPresent = attendanceMap[student.id] ?: false,
                                        onToggle = {
                                            attendanceMap = attendanceMap + (student.id to !(attendanceMap[student.id] ?: false))
                                        }
                                    )
                                }
                            }
                            Spacer(Modifier.height(16.dp))

                            // Action Buttons
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                if (!tripStarted && trip.status == TripStatus.SCHEDULED) {
                                    Button(
                                        onClick = {
                                            tripStarted = true
                                            startTime = Date()
                                            tripViewModel.startTrip(trip.id)
                                        },
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Icon(Icons.Default.PlayArrow, contentDescription = null)
                                        Spacer(Modifier.width(8.dp))
                                        Text("Start Trip")
                                    }
                                } else if (tripStarted && trip.status == TripStatus.IN_PROGRESS) {
                                    Button(
                                        onClick = {
                                            endTime = Date()
                                            tripViewModel.endTrip(trip.id)
                                            // Generate PDF report and send notifications
                                            // This would be handled by the backend
                                        },
                                        modifier = Modifier.weight(1f),
                                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                                    ) {
                                        Icon(Icons.Default.Stop, contentDescription = null)
                                        Spacer(Modifier.width(8.dp))
                                        Text("End Trip")
                                    }
                                }
                            }
                        } ?: run {
                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                Text("Select a trip to view details")
                            }
                        }
                    }
                }
            }
            else -> {
                Box(modifier = Modifier.fillMaxSize().padding(paddingValues), contentAlignment = Alignment.Center) {
                    Text("No trips available")
                }
            }
        }
    }
}

@Composable
fun StudentAttendanceRow(student: Student, isPresent: Boolean, onToggle: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(student.name, style = MaterialTheme.typography.bodyLarge)
                if (student.grade != null) {
                    Text("Grade: ${student.grade}", style = MaterialTheme.typography.bodySmall)
                }
            }
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = isPresent,
                    onClick = onToggle,
                    label = { Text(if (isPresent) "Present" else "Absent") },
                    leadingIcon = if (isPresent) {
                        { Icon(Icons.Default.CheckCircle, contentDescription = null) }
                    } else {
                        { Icon(Icons.Default.Close, contentDescription = null) }
                    }
                )
            }
        }
    }
}

