package com.example.schoolbustransport.presentation.trip

import android.Manifest
import android.view.ViewGroup
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.schoolbustransport.presentation.common.SessionViewModel
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay
@OptIn(ExperimentalMaterial3Api::class, ExperimentalPermissionsApi::class)
@Composable
fun LiveTrackingScreen(
    navController: NavController,
    tripId: String? = null,
    sessionViewModel: SessionViewModel = hiltViewModel()
) {
    var busLocation by remember { mutableStateOf<GeoPoint?>(null) }
    // These would be loaded from trip details (route and students)
    val tripViewModel: TripViewModel = hiltViewModel()
    val selectedTrip by tripViewModel.selectedTrip.collectAsState()
    // Load trip details if tripId is provided

    LaunchedEffect(tripId) {
        tripId?.let { tripViewModel.loadTripDetails(it) }
    }
    // val token by sessionViewModel.tokenFlow.collectAsState(initial = null)

    val locationPermissions = rememberMultiplePermissionsState(
        permissions = listOf(
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION
        )
    )
    // Removed duplicate imports

    // TODO: Implement real-time bus location updates using Firebase (e.g., Firestore or Realtime Database)
    // Removed all SocketManager and BASE_URL logic

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Live Tracking") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        if (locationPermissions.allPermissionsGranted) {
            Column(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
                // Trip Info Card
                selectedTrip?.let { trip ->
                    Card(
                        modifier = Modifier.fillMaxWidth().padding(8.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                    ) {
                        Column(Modifier.padding(12.dp)) {
                            Text(trip.route.name, style = MaterialTheme.typography.titleMedium)
                            Text("Bus: ${trip.bus.licensePlate}", style = MaterialTheme.typography.bodyMedium)
                            Text("Driver: ${trip.driver.name}", style = MaterialTheme.typography.bodyMedium)
                            Text("Status: ${trip.status}", style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }

                // Map and Legend
                Box(modifier = Modifier.weight(1f)) {
                    AndroidView(
                        factory = { context ->
                            MapView(context).apply {
                                layoutParams = ViewGroup.LayoutParams(
                                    ViewGroup.LayoutParams.MATCH_PARENT,
                                    ViewGroup.LayoutParams.MATCH_PARENT
                                )
                                setTileSource(TileSourceFactory.MAPNIK)
                                setMultiTouchControls(true)
                                controller.setZoom(15.0)
                                // Center on bus or route start if available
                                val center = busLocation ?: selectedTrip?.route?.waypoints?.firstOrNull()?.let { GeoPoint(it.lat, it.lng) } ?: GeoPoint(-1.286389, 36.817223)
                                controller.setCenter(center)
                                val myLocationOverlay = MyLocationNewOverlay(GpsMyLocationProvider(context), this)
                                myLocationOverlay.enableMyLocation()
                                myLocationOverlay.enableFollowLocation()
                                overlays.add(myLocationOverlay)
                            }
                        },
                        modifier = Modifier.fillMaxSize(),
                        update = { view ->
                            view.setTileSource(TileSourceFactory.MAPNIK)
                            // Remove old markers and polylines
                            view.overlays.removeAll { it is Marker && (it.title == "Bus" || it.title == "Pickup" || it.title == "Route") }
                            // Add bus marker (custom icon can be set here)
                            busLocation?.let { loc ->
                                val marker = Marker(view)
                                marker.position = loc
                                marker.title = "Bus"
                                marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                                // TODO: marker.icon = ... (set custom bus icon if available)
                                view.overlays.add(marker)
                            }
                            // Add route polyline if available
                            selectedTrip?.route?.waypoints?.takeIf { it.size > 1 }?.let { waypoints ->
                                val polyline = org.osmdroid.views.overlay.Polyline().apply {
                                    setPoints(waypoints.map { GeoPoint(it.lat, it.lng) })
                                    title = "Route"
                                    outlinePaint.color = android.graphics.Color.BLUE
                                    outlinePaint.strokeWidth = 8f // Thicker for visibility
                                }
                                view.overlays.add(polyline)
                            }
                            // Add student pickup markers if available
                            selectedTrip?.students?.forEach { student ->
                                if (student.pickupLat != null && student.pickupLng != null) {
                                    val marker = org.osmdroid.views.overlay.Marker(view)
                                    marker.position = org.osmdroid.util.GeoPoint(student.pickupLat, student.pickupLng)
                                    marker.title = student.name
                                    // Optionally set a custom icon for pickup
                                    // marker.icon = ...
                                    marker.setAnchor(org.osmdroid.views.overlay.Marker.ANCHOR_CENTER, org.osmdroid.views.overlay.Marker.ANCHOR_BOTTOM)
                                    view.overlays.add(marker)
                                }
                            }
                        }
                    )
                    // Map Legend Overlay
                    Column(
                        modifier = Modifier.align(Alignment.TopEnd).padding(12.dp).background(MaterialTheme.colorScheme.surface.copy(alpha = 0.85f), shape = MaterialTheme.shapes.medium).padding(8.dp),
                        horizontalAlignment = Alignment.End
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(Modifier.size(16.dp).background(MaterialTheme.colorScheme.primary, shape = MaterialTheme.shapes.small))
                            Spacer(Modifier.width(4.dp))
                            Text("Bus", style = MaterialTheme.typography.labelSmall)
                        }
                        Spacer(Modifier.height(4.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(Modifier.size(16.dp).background(Color.Blue, shape = MaterialTheme.shapes.small))
                            Spacer(Modifier.width(4.dp))
                            Text("Route", style = MaterialTheme.typography.labelSmall)
                        }
                        Spacer(Modifier.height(4.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(Modifier.size(16.dp).background(Color.Green, shape = MaterialTheme.shapes.small))
                            Spacer(Modifier.width(4.dp))
                            Text("Pickup", style = MaterialTheme.typography.labelSmall)
                        }
                    }
                }
            }
        } else {
            // Permission request overlay
            Box(modifier = Modifier.fillMaxSize().padding(paddingValues), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                    Text("Location permission is needed to show the map.", modifier = Modifier.padding(16.dp))
                    Button(onClick = {
                        locationPermissions.launchMultiplePermissionRequest()
                    }, modifier = Modifier.padding(16.dp)) {
                        Text("Allow Location Access")
                    }
                }
            }
        }
    }
}
