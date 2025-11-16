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
            ExtendedFloatingActionButton(
                text = { Text("Download PDF") },
                icon = { Icon(Icons.Default.CheckCircle, contentDescription = null) },
                onClick = {
                    // Temporarily disabled after migration to Firebase
                    // downloadReport(scope, context, tripId)
                    Toast.makeText(context, "PDF Download is temporarily disabled.", Toast.LENGTH_SHORT).show()
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
                            Card(modifier = Modifier.fillMaxWidth()) {
                                Row(
                                    Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF2E7D32))
                                    Spacer(Modifier.width(8.dp))
                                    val displayName = student.name.ifBlank { "Student ${student.id}" }
                                    Text(displayName)
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
    // This function is temporarily disabled as it depends on the old ApiService.
    // To re-enable, this needs to be implemented with a Firebase Cloud Function.
    /*
    val api = EntryPointAccessors.fromApplication(context.applicationContext, ServiceEntryPoint::class.java).apiService()
    scope.launch(Dispatchers.IO) {
        try {
            val resp = api.getTripReport(tripId)
            val body = resp.body()
            if (resp.isSuccessful && body != null) {
                val pdfBytes = body.bytes()
                val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                val outFile = File(downloadsDir, "trip-report-$tripId.pdf")
                try {
                    outFile.outputStream().use { it.write(pdfBytes) }
                    launch(Dispatchers.Main) {
                        showDownloadNotification(context, outFile.name, outFile.absolutePath)
                        Toast.makeText(context, "PDF saved to Downloads folder", Toast.LENGTH_LONG).show()
                    }
                } catch (e: Exception) {
                    launch(Dispatchers.Main) {
                        Toast.makeText(context, "Failed to save PDF: ${e.message}", Toast.LENGTH_LONG).show()
                    }
                }
            } else {
                launch(Dispatchers.Main) {
                    Toast.makeText(context, "Failed to download PDF: ${resp.message()}", Toast.LENGTH_LONG).show()
                }
            }
        } catch (e: Exception) {
            launch(Dispatchers.Main) {
                Toast.makeText(context, "Failed to download PDF: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }
    */
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
