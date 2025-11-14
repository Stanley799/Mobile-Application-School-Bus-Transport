package com.example.schoolbustransport.presentation.trip

import android.Manifest
import android.view.ViewGroup
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.schoolbustransport.BuildConfig
import com.example.schoolbustransport.data.realtime.SocketManager
import com.example.schoolbustransport.presentation.common.SessionViewModel
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import org.json.JSONObject
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
    val token by sessionViewModel.tokenFlow.collectAsState(initial = null)

    val locationPermissions = rememberMultiplePermissionsState(
        permissions = listOf(
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION
        )
    )

    DisposableEffect(token, tripId) {
        val currentToken = token
        if (currentToken.isNullOrBlank() || tripId.isNullOrBlank()) {
            return@DisposableEffect onDispose {}
        }
        val manager = SocketManager(apiBaseUrl = BuildConfig.BASE_URL, token = currentToken)

        manager.on("location-broadcast") { args ->
            when (val payload = args.firstOrNull()) {
                is JSONObject -> {
                    val lat = runCatching { payload.getDouble("latitude") }.getOrNull()
                    val lng = runCatching { payload.getDouble("longitude") }.getOrNull()
                    if (lat != null && lng != null) busLocation = GeoPoint(lat, lng)
                }
                is Map<*, *> -> {
                    val lat = (payload["latitude"] as? Number)?.toDouble()
                    val lng = (payload["longitude"] as? Number)?.toDouble()
                    if (lat != null && lng != null) busLocation = GeoPoint(lat, lng)
                }
            }
        }
        manager.on("error") { _ -> }

        manager.connect()
        val tripIdInt = tripId.toIntOrNull()
        if (tripIdInt != null) {
            manager.joinTrip(tripIdInt)
        }

        onDispose {
            manager.off("location-broadcast")
            manager.off("error")
            manager.disconnect()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Live Bus Tracking") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues).fillMaxSize()) {
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
                        controller.setCenter(GeoPoint(-1.286389, 36.817223))
                        val myLocationOverlay = MyLocationNewOverlay(GpsMyLocationProvider(context), this)
                        myLocationOverlay.enableMyLocation()
                        myLocationOverlay.enableFollowLocation()
                        overlays.add(myLocationOverlay)
                    }
                },
                modifier = Modifier.fillMaxSize(),
                update = { view ->
                    view.setTileSource(TileSourceFactory.MAPNIK)
                    view.overlays.removeAll { it is Marker && it.title == "Bus" }
                    busLocation?.let { loc ->
                        val marker = Marker(view)
                        marker.position = loc
                        marker.title = "Bus"
                        marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                        view.overlays.add(marker)
                    }
                }
            )
            if (!locationPermissions.allPermissionsGranted) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                        Text("Location permission is needed to show the map.")
                        Button(onClick = { locationPermissions.launchMultiplePermissionRequest() }, modifier = Modifier.padding(16.dp)) {
                            Text("Allow Location Access")
                        }
                    }
                }
            }
        }
    }
}
