package com.example.schoolbustransport.presentation.trip

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.schoolbustransport.data.network.ApiService
import com.example.schoolbustransport.domain.model.Trip
import com.example.schoolbustransport.domain.model.Student
import dagger.hilt.android.EntryPointAccessors
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File

@Composable
fun TripReportScreen(
    tripId: String,
    navController: NavController,
    tripViewModel: TripViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    // Load trip details (with attendance)
    LaunchedEffect(tripId) { tripViewModel.loadTripDetails(tripId) }
    val trip by tripViewModel.selectedTrip.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Trip Report") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.Error, contentDescription = "Back")
                    }
                }
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                text = { Text("Download PDF") },
                icon = { Icon(Icons.Default.CheckCircle, contentDescription = null) },
                onClick = {
                    downloadReport(scope, context, tripId)
                }
            )
        }
    ) { padding ->
        Box(Modifier.fillMaxSize().padding(padding)) {
            val currentTrip = trip
            if (currentTrip == null) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else {
                Column(Modifier.fillMaxSize().padding(16.dp)) {
                    Text("Route: ${currentTrip.route.name}", style = MaterialTheme.typography.titleMedium)
                    Text("Bus: ${currentTrip.bus.licensePlate}")
                    Text("Driver: ${currentTrip.driver.name}")
                    Text("Time: ${currentTrip.departureTime ?: "-"} - ${currentTrip.arrivalTime ?: "-"}")
                    Spacer(Modifier.height(16.dp))
                    Text("Attendance", style = MaterialTheme.typography.titleMedium)
                    Spacer(Modifier.height(8.dp))
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(currentTrip.students) { student ->
                            val attendance = currentTrip.attendance?.find { it.studentId == student.id }
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(
                                    containerColor = if (attendance?.status == "PRESENT") Color(0xFFD0F5E8) else Color(0xFFFFE0E0)
                                )
                            ) {
                                Row(
                                    Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    if (attendance?.status == "PRESENT") {
                                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF2E7D32))
                                    } else {
                                        Icon(Icons.Default.Error, contentDescription = null, tint = Color(0xFFC62828))
                                    }
                                    Spacer(Modifier.width(8.dp))
                                    Text(student.name)
                                    Spacer(Modifier.weight(1f))
                                    Text(attendance?.status ?: "-", color = Color.Gray)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun downloadReport(scope: CoroutineScope, context: Context, tripId: String) {
    val api = EntryPointAccessors.fromApplication(context, com.example.schoolbustransport.presentation.trip.ServiceEntryPoint::class.java).apiService()
    scope.launch(Dispatchers.IO) {
        val resp = api.getTripReport(tripId)
        if (resp.isSuccessful && resp.body() != null) {
            val pdfBytes = resp.body()!!.bytes()
            val outFile = File(context.filesDir, "trip-report-$tripId.pdf")
            outFile.outputStream().use { it.write(pdfBytes) }
            launch(Dispatchers.Main) {
                Toast.makeText(context, "PDF saved: ${outFile.absolutePath}", Toast.LENGTH_LONG).show()
            }
        } else {
            launch(Dispatchers.Main) {
                Toast.makeText(context, "Failed to download PDF", Toast.LENGTH_LONG).show()
            }
        }
    }
}

// Hilt entry point to resolve ApiService outside of @AndroidEntryPoint scope
@dagger.hilt.EntryPoint
@dagger.hilt.InstallIn(dagger.hilt.components.SingletonComponent::class)
interface ServiceEntryPoint {
    fun apiService(): ApiService
}
