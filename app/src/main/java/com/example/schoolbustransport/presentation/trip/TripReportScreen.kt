package com.example.schoolbustransport.presentation.trip

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.os.Environment
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.app.NotificationCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.schoolbustransport.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import com.example.schoolbustransport.data.repository.TripReportRepository

@OptIn(ExperimentalMaterial3Api::class)
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
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        floatingActionButton = {
            var downloading by remember { mutableStateOf(false) }
            ExtendedFloatingActionButton(
                text = { Text(if (downloading) "Downloading..." else "Download PDF") },
                icon = { 
                    if (downloading) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp))
                    } else {
                        Icon(Icons.Default.CheckCircle, contentDescription = null)
                    }
                },
                onClick = {
                    downloading = true
                    scope.launch {
                        downloadReportFromFirebase(context, tripId) { success ->
                            downloading = false
                            if (success) {
                                Toast.makeText(context, "PDF downloaded successfully", Toast.LENGTH_SHORT).show()
                            } else {
                                Toast.makeText(context, "Failed to download PDF. Report may not be ready yet.", Toast.LENGTH_LONG).show()
                            }
                        }
                    }
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
                    Text(
                        currentTrip.tripName.ifBlank { currentTrip.id.ifBlank { "Trip Report" } },
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                    )
                    Spacer(Modifier.height(16.dp))
                    
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(Modifier.padding(16.dp)) {
                            Text("Trip Details", style = MaterialTheme.typography.titleMedium, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
                            Spacer(Modifier.height(8.dp))
                            Text("Route: ${currentTrip.route.name}")
                    Text("Bus: ${currentTrip.bus.licensePlate}")
                    Text("Driver: ${currentTrip.driver.name}")
                            Text("Departure: ${currentTrip.departureTime ?: "-"}")
                            if (currentTrip.startTime != null) {
                                Text("Start Time: ${java.text.SimpleDateFormat("yyyy-MM-dd HH:mm", java.util.Locale.getDefault()).format(currentTrip.startTime.toDate())}")
                            }
                            if (currentTrip.endTime != null) {
                                Text("End Time: ${java.text.SimpleDateFormat("yyyy-MM-dd HH:mm", java.util.Locale.getDefault()).format(currentTrip.endTime.toDate())}")
                            }
                            Text("Status: ${currentTrip.status.name}")
                        }
                    }
                    
                    Spacer(Modifier.height(16.dp))
                    Text("Attendance List", style = MaterialTheme.typography.titleMedium, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
                    Spacer(Modifier.height(8.dp))
                    
                    if (currentTrip.students.isEmpty()) {
                        Text("No students on this trip", style = MaterialTheme.typography.bodyMedium)
                    } else {
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(currentTrip.students) { student ->
                            Card(modifier = Modifier.fillMaxWidth()) {
                                Row(
                                    Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                        Icon(
                                            Icons.Default.CheckCircle,
                                            contentDescription = null,
                                            tint = Color(0xFF2E7D32),
                                            modifier = Modifier.size(24.dp)
                                        )
                                        Spacer(Modifier.width(12.dp))
                                        Column {
                                    val displayName = student.name.ifBlank { "Student ${student.id}" }
                                            Text(displayName, style = MaterialTheme.typography.bodyLarge)
                                            if (student.grade != null) {
                                                Text("Grade: ${student.grade}", style = MaterialTheme.typography.bodySmall)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun downloadReportFromFirebase(
    context: Context,
    tripId: String,
    onComplete: (Boolean) -> Unit
) {
    val repository = TripReportRepository(
        com.google.firebase.firestore.FirebaseFirestore.getInstance(),
        com.google.firebase.storage.FirebaseStorage.getInstance(),
        com.google.firebase.auth.FirebaseAuth.getInstance()
    )
    
    CoroutineScope(Dispatchers.IO).launch {
        try {
            val report = repository.getTripReport(tripId)
            if (report == null) {
                launch(Dispatchers.Main) {
                    onComplete(false)
                }
                return@launch
            }
            
            val downloadsDir = android.os.Environment.getExternalStoragePublicDirectory(android.os.Environment.DIRECTORY_DOWNLOADS)
                val outFile = File(downloadsDir, "trip-report-$tripId.pdf")
            
            val result = repository.downloadReport(report, outFile)
            result.onSuccess {
                    launch(Dispatchers.Main) {
                        showDownloadNotification(context, outFile.name, outFile.absolutePath)
                    onComplete(true)
                    }
            }.onFailure {
                    launch(Dispatchers.Main) {
                    onComplete(false)
                }
            }
        } catch (e: Exception) {
            launch(Dispatchers.Main) {
                onComplete(false)
            }
        }
    }
}

private fun showDownloadNotification(context: Context, fileName: String, filePath: String) {
    val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    val channelId = "download_channel"

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        val channel = NotificationChannel(channelId, "Downloads", NotificationManager.IMPORTANCE_DEFAULT)
        notificationManager.createNotificationChannel(channel)
    }

    val notification = NotificationCompat.Builder(context, channelId)
        .setContentTitle("Download Complete")
        .setContentText("$fileName saved to $filePath")
        .setSmallIcon(R.drawable.ic_launcher_foreground)
        .build()

    notificationManager.notify(1, notification)
}
