package com.example.cleantrack.view.admin

import android.app.Activity
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Undo
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.cleantrack.model.map.LatLngPoint
import com.example.cleantrack.model.map.RouteModel
import com.example.cleantrack.repository.RouteRepoImpl
import com.example.cleantrack.ui.theme.Green
import com.example.cleantrack.ui.theme.White
import com.example.cleantrack.util.ApiTokenUtil
import com.example.cleantrack.viewmodel.RouteViewModel
import kotlinx.coroutines.delay
import org.maplibre.android.MapLibre
import org.maplibre.android.annotations.Polyline
import org.maplibre.android.annotations.PolylineOptions
import org.maplibre.android.camera.CameraPosition
import org.maplibre.android.camera.CameraUpdateFactory
import org.maplibre.android.geometry.LatLng
import org.maplibre.android.geometry.LatLngBounds
import org.maplibre.android.maps.MapLibreMap
import org.maplibre.android.maps.MapView

class AdminRouteSetupActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val routeId = intent.getStringExtra("ROUTE_ID")
        setContent {
            AdminRouteSetupScreen(savedInstanceState = savedInstanceState, routeId = routeId)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminRouteSetupScreen(savedInstanceState: Bundle?, routeId: String?) {
    val context = LocalContext.current
    val activity = context as Activity
    MapLibre.getInstance(context.applicationContext)

    val viewModel = remember { RouteViewModel(RouteRepoImpl()) }
    val selectedRoute by viewModel.route.observeAsState()
    val loading by viewModel.loading.observeAsState(false)

    // Local state for button loading
    var isSaving by remember { mutableStateOf(false) }

    var routeName by remember { mutableStateOf("") }
    var points by remember { mutableStateOf<List<LatLng>>(emptyList()) }
    var baatoApiKey by remember { mutableStateOf<String?>(null) }
    var isMapStyleLoaded by remember { mutableStateOf(false) }

    val mapView = remember { MapView(context).apply { onCreate(savedInstanceState) } }
    var map by remember { mutableStateOf<MapLibreMap?>(null) }
    var polyline by remember { mutableStateOf<Polyline?>(null) }

    // Load Data
    LaunchedEffect(routeId) { routeId?.let { viewModel.getRouteById(it) } }
    LaunchedEffect(Unit) { baatoApiKey = ApiTokenUtil.getBaatoApiKey() }

    LaunchedEffect(selectedRoute) {
        selectedRoute?.let { route ->
            routeName = route.name
            points = route.points.map { LatLng(it.lat, it.lon) }
        }
    }

    // Map Setup
    DisposableEffect(mapView) {
        mapView.getMapAsync { m -> map = m }
        onDispose { mapView.onDestroy() }
    }

    LaunchedEffect(map, baatoApiKey) {
        val mapInstance = map
        val apiKey = baatoApiKey
        if (mapInstance == null || apiKey.isNullOrBlank()) return@LaunchedEffect

        val styleUrl = "https://api.baato.io/api/v1/styles/breeze_cdn?key=$apiKey"
        mapInstance.setStyle(styleUrl) {
            isMapStyleLoaded = true
            mapInstance.cameraPosition = CameraPosition.Builder()
                .target(LatLng(27.7172, 85.3240))
                .zoom(12.0)
                .build()

            mapInstance.addOnMapClickListener { latLng ->
                points = points + latLng
                true
            }
        }
    }

    // Auto-Zoom Logic
    LaunchedEffect(isMapStyleLoaded, points) {
        val currentMap = map
        if (isMapStyleLoaded && currentMap != null && points.size >= 2 && routeId != null) {
            delay(500)
            try {
                val bounds = LatLngBounds.Builder().includes(points).build()
                currentMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, 200))
            } catch (e: Exception) {
                currentMap.animateCamera(CameraUpdateFactory.newLatLngZoom(points.first(), 14.0))
            }
        }
    }

    // Polyline Sync
    LaunchedEffect(points, map) {
        map?.let { m ->
            polyline?.let { m.removePolyline(it) }
            if (points.size >= 2) {
                polyline = m.addPolyline(PolylineOptions()
                    .addAll(points)
                    .color(android.graphics.Color.parseColor("#4CAF50"))
                    .width(6f))
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize().background(Brush.verticalGradient(colors = listOf(Green, White), startY = 0f, endY = 800f))) {
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                CenterAlignedTopAppBar(
                    title = { Text(if (routeId == null) "Create Route" else "Edit Path", color = White, fontWeight = FontWeight.Bold, fontSize = 20.sp) },
                    navigationIcon = {
                        IconButton(onClick = { activity.finish() }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = White)
                        }
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.Transparent)
                )
            }
        ) { pad ->
            if (loading) {
                Box(modifier = Modifier.fillMaxSize().padding(pad), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = White)
                }
            } else {
                Column(modifier = Modifier.fillMaxSize().padding(pad)) {
                    Card(
                        modifier = Modifier.weight(1f).fillMaxWidth().padding(16.dp),
                        shape = RoundedCornerShape(24.dp),
                        elevation = CardDefaults.cardElevation(8.dp)
                    ) {
                        Box(modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(24.dp))) {
                            AndroidView(factory = { mapView }, modifier = Modifier.fillMaxSize())
                        }
                    }

                    Card(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(containerColor = White),
                        elevation = CardDefaults.cardElevation(10.dp)
                    ) {
                        Column(Modifier.padding(20.dp)) {
                            OutlinedTextField(
                                value = routeName,
                                onValueChange = { routeName = it },
                                label = { Text("Route Name") },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Green, focusedLabelColor = Green),
                                enabled = !isSaving
                            )

                            Spacer(Modifier.height(16.dp))

                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                OutlinedButton(
                                    modifier = Modifier.weight(1f),
                                    onClick = { if (points.isNotEmpty()) points = points.dropLast(1) },
                                    shape = RoundedCornerShape(12.dp),
                                    enabled = !isSaving
                                ) {
                                    Icon(Icons.Default.Undo, null, modifier = Modifier.size(18.dp))
                                    Text(" Undo")
                                }
                                OutlinedButton(
                                    modifier = Modifier.weight(1f),
                                    onClick = { points = emptyList() },
                                    shape = RoundedCornerShape(12.dp),
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Red),
                                    enabled = !isSaving
                                ) {
                                    Icon(Icons.Default.DeleteSweep, null, modifier = Modifier.size(18.dp))
                                    Text(" Clear")
                                }
                            }

                            Spacer(Modifier.height(16.dp))

                            Button(
                                modifier = Modifier.fillMaxWidth().height(56.dp),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Green),
                                enabled = !isSaving, // Disable when loading
                                onClick = {
                                    if (routeName.isBlank() || points.size < 2) {
                                        Toast.makeText(context, "Please complete route info", Toast.LENGTH_SHORT).show()
                                        return@Button
                                    }

                                    isSaving = true // Start Loading

                                    val model = RouteModel(
                                        routeId = routeId ?: "",
                                        name = routeName.trim(),
                                        points = points.map { LatLngPoint(it.latitude, it.longitude) }
                                    )

                                    if (routeId == null) {
                                        viewModel.addRoute(model) { ok, msg ->
                                            isSaving = false // Stop Loading
                                            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                                            if (ok) activity.finish()
                                        }
                                    } else {
                                        viewModel.updateRoute(model) { ok, msg ->
                                            isSaving = false // Stop Loading
                                            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                                            if (ok) activity.finish()
                                        }
                                    }
                                }
                            ) {
                                if (isSaving) {
                                    // Show loading indicator inside button
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(24.dp),
                                        color = White,
                                        strokeWidth = 2.dp
                                    )
                                } else {
                                    Text(
                                        if (routeId == null) "Save Route" else "Update Route",
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}