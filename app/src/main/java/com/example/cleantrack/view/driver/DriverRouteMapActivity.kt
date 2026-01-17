package com.example.cleantrack.view.driver

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
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.cleantrack.repository.RouteRepoImpl
import com.example.cleantrack.repository.ScheduleRepoImpl
import com.example.cleantrack.repository.UserRepoImpl
import com.example.cleantrack.util.ApiTokenUtil
import com.example.cleantrack.viewmodel.RouteViewModel
import com.example.cleantrack.viewmodel.ScheduleViewModel
import com.example.cleantrack.viewmodel.UserViewModel
import org.maplibre.android.MapLibre
import org.maplibre.android.annotations.Polyline
import org.maplibre.android.annotations.PolylineOptions
import org.maplibre.android.camera.CameraUpdateFactory
import org.maplibre.android.geometry.LatLng
import org.maplibre.android.maps.MapLibreMap
import org.maplibre.android.maps.MapView

class DriverRouteMapActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            DriverRouteMapScreen(savedInstanceState)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DriverRouteMapScreen(savedInstanceState: Bundle?) {
    val context = LocalContext.current
    MapLibre.getInstance(context.applicationContext)

    // ViewModels
    val userVM = remember { UserViewModel(UserRepoImpl()) }
    val scheduleVM = remember { ScheduleViewModel(ScheduleRepoImpl()) }
    val routeVM = remember { RouteViewModel(RouteRepoImpl()) }

    // Observers
    val assignedSchedule by scheduleVM.schedule.observeAsState()
    val allRoutes by routeVM.routes.observeAsState(emptyList())

    // Map States
    val mapView = remember { MapView(context).apply { onCreate(savedInstanceState) } }
    var mapInstance by remember { mutableStateOf<MapLibreMap?>(null) }
    var currentPolyline by remember { mutableStateOf<Polyline?>(null) }
    var baatoApiKey by remember { mutableStateOf<String?>(null) }

    // 1. Initial Fetch: Get Driver's assigned Schedule
    LaunchedEffect(Unit) {
        val uid = userVM.getCurrentUserId()
        if (uid != null) {
            scheduleVM.getScheduleByDriver(uid)
            routeVM.loadRoutes() // Load all routes so we can pick the assigned one
        }
    }

    LaunchedEffect(Unit) {
        baatoApiKey = ApiTokenUtil.getBaatoApiKey()
    }

    LaunchedEffect(mapInstance, baatoApiKey) {
        val map = mapInstance
        val apiKey = baatoApiKey
        if (map == null || apiKey.isNullOrBlank()) {
            return@LaunchedEffect
        }
        val styleUrl = "https://api.baato.io/api/v1/styles/breeze_cdn?key=$apiKey"
        map.setStyle(styleUrl)
    }

    // 2. Logic: Draw the specific route assigned to this driver
    LaunchedEffect(assignedSchedule, allRoutes, mapInstance) {
        val routeId = assignedSchedule?.routeId
        val map = mapInstance

        if (routeId != null && allRoutes.isNotEmpty() && map != null) {
            val driverRoute = allRoutes.find { it.routeId == routeId }

            if (driverRoute != null) {
                // Clear existing lines
                currentPolyline?.let { map.removePolyline(it) }

                // Map points to LatLng
                val latLngs = driverRoute.points.map { LatLng(it.lat, it.lon) }

                if (latLngs.isNotEmpty()) {
                    // Draw Polyline
                    currentPolyline = map.addPolyline(
                        PolylineOptions()
                            .addAll(latLngs)
                            .color(android.graphics.Color.parseColor("#4CAF50"))
                            .width(6f)
                    )

                    // Focus camera on the route
                    map.animateCamera(CameraUpdateFactory.newLatLngZoom(latLngs[0], 14.0))
                }
            } else {
                Toast.makeText(context, "Assigned route details not found.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Assigned Route", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        assignedSchedule?.let {
                            Text(it.routeName, style = MaterialTheme.typography.labelSmall)
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { (context as? Activity)?.finish() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { pad ->
        Box(modifier = Modifier.fillMaxSize().padding(pad)) {
            AndroidView(
                factory = { mapView },
                modifier = Modifier.fillMaxSize()
            ) { view ->
                view.getMapAsync { m ->
                    mapInstance = m

                    // Optional: Enable current location dot
                    m.uiSettings.isCompassEnabled = true
                }
            }
        }
    }
}
