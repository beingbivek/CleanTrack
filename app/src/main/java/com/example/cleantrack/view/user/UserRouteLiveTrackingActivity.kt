package com.example.cleantrack.view.user

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
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.cleantrack.model.map.RouteModel
import com.example.cleantrack.repository.RouteRepoImpl
import com.example.cleantrack.repository.UserRepoImpl
import com.example.cleantrack.ui.theme.*
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

    val routeVM = remember { RouteViewModel(RouteRepoImpl()) }
    val userVM = remember { UserViewModel(UserRepoImpl()) }

    val routes by routeVM.routes.observeAsState(emptyList())
    val userProfile by userVM.user.observeAsState()

    var expanded by remember { mutableStateOf(false) }
    var selectedRoute by remember { mutableStateOf<RouteModel?>(null) }
    val mapView = remember { MapView(context).apply { onCreate(savedInstanceState) } }
    var mapInstance by remember { mutableStateOf<MapLibreMap?>(null) }
    var currentPolyline by remember { mutableStateOf<Polyline?>(null) }
    var baatoApiKey by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        routeVM.loadRoutes()
        userVM.getCurrentUserId()?.let { uid -> userVM.getUserById(uid) }
        baatoApiKey = ApiTokenUtil.getBaatoApiKey()
    }

    ApplyBaatoStyle(mapInstance, baatoApiKey)

    LaunchedEffect(routes, userProfile) {
        val uLat = userProfile?.latitude
        val uLon = userProfile?.longitude
        val savedRouteId = userProfile?.activeRouteId

        if (routes.isNotEmpty() && selectedRoute == null) {
            val savedRoute = routes.find { it.routeId == savedRouteId }
            if (savedRoute != null) {
                selectedRoute = savedRoute
            } else if (uLat != null && uLon != null) {
                selectedRoute = routes.minByOrNull { route ->
                    route.points.minOf { pt -> calculateDistance(uLat, uLon, pt.lat, pt.lon) }
                }
            }
        }
    }

    LaunchedEffect(selectedRoute, mapInstance) {
        mapInstance?.let { m ->
            currentPolyline?.let { m.removePolyline(it) }
            selectedRoute?.let { route ->
                val latLngs = route.points.map { LatLng(it.lat, it.lon) }
                if (latLngs.isNotEmpty()) {
                    currentPolyline = m.addPolyline(PolylineOptions()
                        .addAll(latLngs)
                        .color(android.graphics.Color.parseColor("#4CAF50")) // CleanTrack Green
                        .width(8f))
                    m.animateCamera(CameraUpdateFactory.newLatLngZoom(latLngs[0], 14.0))
                }
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(Blue, Green, Color.White), endY = 1100f))
    ) {
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                CenterAlignedTopAppBar(
                    title = { Text("Route Selection", fontWeight = FontWeight.ExtraBold, color = Color.White) },
                    navigationIcon = {
                        IconButton(onClick = { (context as? Activity)?.finish() }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = Color.White)
                        }
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.Transparent)
                )
            }
        ) { pad ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(pad)
            ) {
                // Info Section
                Card(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.9f))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.LocationOn, null, tint = Green, modifier = Modifier.size(20.dp))
                            Spacer(Modifier.width(8.dp))
                            Text(
                                "Neighborhood Tracking",
                                fontWeight = FontWeight.Bold,
                                color = Black,
                                fontSize = 16.sp
                            )
                        }
                        Text(
                            "Select a route to view the collection path on the map.",
                            color = Color.Gray,
                            fontSize = 13.sp,
                            modifier = Modifier.padding(top = 4.dp)
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // DROPDOWN
                        ExposedDropdownMenuBox(
                            expanded = expanded,
                            onExpandedChange = { expanded = !expanded }
                        ) {
                            OutlinedTextField(
                                value = selectedRoute?.name ?: "Select a Route",
                                onValueChange = {},
                                readOnly = true,
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
                                modifier = Modifier.fillMaxWidth().menuAnchor(),
                                shape = RoundedCornerShape(12.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = Green,
                                    unfocusedBorderColor = Color.LightGray
                                )
                            )
                            ExposedDropdownMenu(
                                expanded = expanded,
                                onDismissRequest = { expanded = false },
                                modifier = Modifier.background(Color.White)
                            ) {
                                routes.forEach { route ->
                                    DropdownMenuItem(
                                        text = { Text(route.name, fontWeight = FontWeight.Medium) },
                                        onClick = {
                                            selectedRoute = route
                                            expanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                }

                // MAP AREA
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(horizontal = 16.dp)
                        .padding(bottom = 16.dp),
                    shape = RoundedCornerShape(24.dp),
                    elevation = CardDefaults.cardElevation(8.dp)
                ) {
                    Box(modifier = Modifier.fillMaxSize()) {
                        AndroidView(
                            factory = { mapView },
                            modifier = Modifier.fillMaxSize()
                        ) { view ->
                            view.getMapAsync { m -> mapInstance = m }
                        }
                    }
                }

                // CONFIRM BUTTON
                if (selectedRoute != null) {
                    Button(
                        onClick = {
                            val uid = userVM.getCurrentUserId()
                            if (uid != null) {
                                userVM.updateActiveRoute(uid, selectedRoute!!.routeId)
                                Toast.makeText(context, "Route saved successfully", Toast.LENGTH_SHORT).show()
                                (context as? Activity)?.finish()
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 32.dp, vertical = 24.dp)
                            .height(56.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Green),
                        elevation = ButtonDefaults.buttonElevation(4.dp)
                    ) {
                        Text("CONFIRM THIS ROUTE", fontWeight = FontWeight.Black, letterSpacing = 1.sp)
                    }
                }
            }
        }
    }
}

@Composable
private fun ApplyBaatoStyle(mapInstance: MapLibreMap?, baatoApiKey: String?) {
    LaunchedEffect(mapInstance, baatoApiKey) {
        if (mapInstance == null || baatoApiKey.isNullOrBlank()) return@LaunchedEffect
        val styleUrl = "https://api.baato.io/api/v1/styles/breeze_cdn?key=$baatoApiKey"
        mapInstance.setStyle(styleUrl)
    }
}

fun calculateDistance(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
    val r = 6371
    val dLat = Math.toRadians(lat2 - lat1)
    val dLon = Math.toRadians(lon2 - lon1)
    val a = sin(dLat / 2).pow(2) + cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) * sin(dLon / 2).pow(2)
    val c = 2 * atan2(sqrt(a), sqrt(1 - a))
    return r * c
}