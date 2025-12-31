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
import androidx.compose.ui.unit.sp
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

    // 2. Logic: Load saved route OR use Recommendation Logic
    LaunchedEffect(routes, userProfile) {
        val uLat = userProfile?.latitude
        val uLon = userProfile?.longitude
        val savedRouteId = userProfile?.activeRouteId // Get saved route from profile

        if (routes.isNotEmpty() && selectedRoute == null) {
            // Priority 1: Use the route the user saved previously
            val savedRoute = routes.find { it.routeId == savedRouteId }

            if (savedRoute != null) {
                selectedRoute = savedRoute
            } else if (uLat != null && uLon != null) {
                // Priority 2: Use recommendation based on distance
                selectedRoute = routes.minByOrNull { route ->
                    route.points.minOf { pt ->
                        calculateDistance(uLat, uLon, pt.lat, pt.lon)
                    }
                }
                Toast.makeText(context, "Recommended: ${selectedRoute?.name}", Toast.LENGTH_SHORT).show()
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
        // Box allows us to stack the Confirm Button over the Map
        Box(modifier = Modifier.fillMaxSize().padding(pad)) {
            Column(modifier = Modifier.fillMaxSize()) {

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

            // --- CONFIRMATION BUTTON ---
            if (selectedRoute != null) {
                Button(
                    onClick = {
                        val uid = userVM.getCurrentUserId()
                        if (uid != null) {
                            userVM.updateActiveRoute(uid, selectedRoute!!.routeId)
                            Toast.makeText(context, "Route confirmed and saved!", Toast.LENGTH_SHORT).show()
                        }
                    },
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 32.dp)
                        .fillMaxWidth(0.8f)
                        .height(56.dp),
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(28.dp),
                    elevation = ButtonDefaults.buttonElevation(8.dp)
                ) {
                    Text("Confirm Route", fontSize = 16.sp, fontWeight = FontWeight.Bold)
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

