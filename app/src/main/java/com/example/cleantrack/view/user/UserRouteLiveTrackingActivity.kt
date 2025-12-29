package com.example.cleantrack.view.user

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.cleantrack.model.map.RouteModel
import com.example.cleantrack.repository.RouteRepoImpl
import com.example.cleantrack.util.ApiTokenUtil
import com.example.cleantrack.viewmodel.RouteViewModel
import org.maplibre.android.MapLibre
import org.maplibre.android.annotations.Polyline
import org.maplibre.android.annotations.PolylineOptions
import org.maplibre.android.camera.CameraUpdateFactory
import org.maplibre.android.geometry.LatLng
import org.maplibre.android.maps.MapLibreMap
import org.maplibre.android.maps.MapView
import kotlin.math.*

class UserRouteLiveTrackingActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            UserLiveTrackingScreen(savedInstanceState)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserLiveTrackingScreen(savedInstanceState: Bundle?) {
    val context = LocalContext.current
    MapLibre.getInstance(context.applicationContext)

    val routeVM = remember { RouteViewModel(RouteRepoImpl()) }
    val routes by routeVM.routes.observeAsState(emptyList())

    // UI States
    var expanded by remember { mutableStateOf(false) }
    var selectedRoute by remember { mutableStateOf<RouteModel?>(null) }

    // Map States
    val styleUrl = "https://api.baato.io/api/v1/styles/breeze_cdn?key=${ApiTokenUtil.BAATO_API_KEY}"
    val mapView = remember { MapView(context).apply { onCreate(savedInstanceState) } }
    var mapInstance by remember { mutableStateOf<MapLibreMap?>(null) }
    var currentPolyline by remember { mutableStateOf<Polyline?>(null) }

    // Mock User Location (In a real app, fetch this from UserProfile or GPS)
    val userLat = 27.7007
    val userLon = 85.3001

    // 1. Fetch Routes
    LaunchedEffect(Unit) {
        routeVM.loadRoutes()
    }

    // 2. Recommendation Logic: Find closest route to user
    LaunchedEffect(routes) {
        if (routes.isNotEmpty() && selectedRoute == null) {
            selectedRoute = routes.minByOrNull { route ->
                route.points.minOf { pt ->
                    calculateDistance(userLat, userLon, pt.lat, pt.lon)
                }
            }
            Toast.makeText(context, "Recommended: ${selectedRoute?.name}", Toast.LENGTH_SHORT).show()
        }
    }

    // 3. Update Map when route changes
    LaunchedEffect(selectedRoute, mapInstance) {
        mapInstance?.let { m ->
            currentPolyline?.let { m.removePolyline(it) }
            selectedRoute?.let { route ->
                val latLngs = route.points.map { LatLng(it.lat, it.lon) }
                if (latLngs.isNotEmpty()) {
                    currentPolyline = m.addPolyline(PolylineOptions().addAll(latLngs).width(5f))
                    // Zoom to the first point of the route
                    m.animateCamera(CameraUpdateFactory.newLatLngZoom(latLngs[0], 14.0))
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Live Tracking", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { (context as? ComponentActivity)?.finish() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = null)
                    }
                }
            )
        }
    ) { pad ->
        Column(modifier = Modifier.fillMaxSize().padding(pad)) {

            // DROPDOWN SELECTOR
            Box(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = !expanded }
                ) {
                    OutlinedTextField(
                        value = selectedRoute?.name ?: "Select a Route",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Active Waste Collection Route") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
                        modifier = Modifier.fillMaxWidth().menuAnchor()
                    )
                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        routes.forEach { route ->
                            DropdownMenuItem(
                                text = { Text(route.name) },
                                onClick = {
                                    selectedRoute = route
                                    expanded = false
                                }
                            )
                        }
                    }
                }
            }

            // MAP VIEW
            Box(modifier = Modifier.fillMaxSize().weight(1f)) {
                AndroidView(
                    factory = { mapView },
                    modifier = Modifier.fillMaxSize()
                ) {
                    it.getMapAsync { m ->
                        mapInstance = m
                        m.setStyle(styleUrl)
                    }
                }
            }
        }
    }
}

/**
 * Haversine formula to calculate distance between two coordinates in km
 */
fun calculateDistance(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
    val r = 6371 // Earth radius in km
    val dLat = Math.toRadians(lat2 - lat1)
    val dLon = Math.toRadians(lon2 - lon1)
    val a = sin(dLat / 2).pow(2) + cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) * sin(dLon / 2).pow(2)
    val c = 2 * atan2(sqrt(a), sqrt(1 - a))
    return r * c
}

