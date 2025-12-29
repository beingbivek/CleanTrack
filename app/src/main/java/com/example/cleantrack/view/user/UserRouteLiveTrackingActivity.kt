package com.example.cleantrack.view.user

import android.app.Activity
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
import com.example.cleantrack.repository.UserRepoImpl
import com.example.cleantrack.util.ApiTokenUtil
import com.example.cleantrack.viewmodel.RouteViewModel
import com.example.cleantrack.viewmodel.UserViewModel
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

    // ViewModels
    val routeVM = remember { RouteViewModel(RouteRepoImpl()) }
    val userVM = remember { UserViewModel(UserRepoImpl()) }

    // Observers
    val routes by routeVM.routes.observeAsState(emptyList())
    val userProfile by userVM.user.observeAsState()

    // UI & Map States
    var expanded by remember { mutableStateOf(false) }
    var selectedRoute by remember { mutableStateOf<RouteModel?>(null) }
    val mapView = remember { MapView(context).apply { onCreate(savedInstanceState) } }
    var mapInstance by remember { mutableStateOf<MapLibreMap?>(null) }
    var currentPolyline by remember { mutableStateOf<Polyline?>(null) }

    // 1. Fetch User Data and Routes
    LaunchedEffect(Unit) {
        routeVM.loadRoutes()
        userVM.getCurrentUserId()?.let { uid ->
            userVM.getUserById(uid)
        }
    }

    // 2. Recommendation Logic: Compare User Location vs Route Points
    LaunchedEffect(routes, userProfile) {
        val uLat = userProfile?.latitude
        val uLon = userProfile?.longitude

        if (routes.isNotEmpty() && uLat != null && uLon != null && selectedRoute == null) {
            // Find the route that has the closest single point to the user's home
            val recommended = routes.minByOrNull { route ->
                route.points.minOf { pt ->
                    calculateDistance(uLat, uLon, pt.lat, pt.lon)
                }
            }

            selectedRoute = recommended
            recommended?.let {
                Toast.makeText(context, "Recommended Route: ${it.name}", Toast.LENGTH_LONG).show()
            }
        }
    }

    // 3. Update Map Graphics
    LaunchedEffect(selectedRoute, mapInstance) {
        mapInstance?.let { m ->
            currentPolyline?.let { m.removePolyline(it) }
            selectedRoute?.let { route ->
                val latLngs = route.points.map { LatLng(it.lat, it.lon) }
                if (latLngs.isNotEmpty()) {
                    currentPolyline = m.addPolyline(PolylineOptions()
                        .addAll(latLngs)
                        .color(android.graphics.Color.parseColor("#4CAF50"))
                        .width(6f))

                    // Zoom to show the route
                    m.animateCamera(CameraUpdateFactory.newLatLngZoom(latLngs[0], 13.0))
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Live Tracking", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { (context as? Activity)?.finish() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = null)
                    }
                }
            )
        }
    ) { pad ->
        Column(modifier = Modifier.fillMaxSize().padding(pad)) {

            // RECOMMENDATION INFO HEADER
            if (userProfile?.latitude != null) {
                Surface(
                    color = MaterialTheme.colorScheme.primaryContainer,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Showing routes near your saved location",
                        modifier = Modifier.padding(8.dp),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }

            // DROPDOWN
            Box(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = !expanded }
                ) {
                    OutlinedTextField(
                        value = selectedRoute?.name ?: "Select a Route",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Waste Collection Route") },
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

            // MAP
            Box(modifier = Modifier.fillMaxSize().weight(1f)) {
                AndroidView(
                    factory = { mapView },
                    modifier = Modifier.fillMaxSize()
                ) { view ->
                    view.getMapAsync { m ->
                        mapInstance = m
                        val styleUrl = "https://api.baato.io/api/v1/styles/breeze_cdn?key=${ApiTokenUtil.BAATO_API_KEY}"
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

