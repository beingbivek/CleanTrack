package com.example.cleantrack.view.admin

import android.os.Bundle
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.cleantrack.model.map.LatLngPoint
import com.example.cleantrack.model.map.RouteModel
import com.example.cleantrack.ui.theme.TextBoxColor
import com.example.cleantrack.util.ApiTokenUtil
import com.example.cleantrack.viewModel.RouteViewModel
import org.maplibre.android.MapLibre
import org.maplibre.android.annotations.Polyline
import org.maplibre.android.annotations.PolylineOptions
import org.maplibre.android.camera.CameraPosition
import org.maplibre.android.geometry.LatLng
import org.maplibre.android.maps.MapLibreMap
import org.maplibre.android.maps.MapView

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminRouteSetupScreen(
    savedInstanceState: Bundle?,
    routeViewModel: RouteViewModel
) {
    val context = LocalContext.current
    MapLibre.getInstance(context.applicationContext)

    var routeName by remember { mutableStateOf("") }
    var points by remember { mutableStateOf<List<LatLng>>(emptyList()) }

    val mapView = remember { MapView(context).apply { onCreate(savedInstanceState) } }
    val styleUrl = "https://api.baato.io/api/v1/styles/breeze_cdn?key=${ApiTokenUtil.BAATO_API_KEY}"

    var map by remember { mutableStateOf<MapLibreMap?>(null) }
    var polyline by remember { mutableStateOf<Polyline?>(null) }

    LaunchedEffect(Unit) { routeViewModel.startRoutesListener() }

    DisposableEffect(mapView) {
        mapView.getMapAsync { m ->
            map = m
            m.setStyle(styleUrl) {
                val center = LatLng(27.7172, 85.3240)
                m.cameraPosition = CameraPosition.Builder().target(center).zoom(12.0).build()

                // Tap to add point
                m.addOnMapClickListener { latLng ->
                    points = points + latLng
                    // redraw polyline
                    polyline?.let { m.removePolyline(it) }
                    if (points.size >= 2) {
                        polyline = m.addPolyline(
                            PolylineOptions().addAll(points)
                        )
                    }
                    true
                }
            }
        }
        onDispose {
            mapView.onDestroy()
        }
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Admin: Route Setup") }) }
    ) { pad ->
        Column(Modifier.fillMaxSize().padding(pad)) {

            Box(Modifier.fillMaxWidth().weight(1f)) {
                AndroidView(factory = { mapView }, modifier = Modifier.fillMaxSize())
            }

            Card(
                modifier = Modifier.fillMaxWidth().padding(10.dp),
                colors = CardDefaults.cardColors(containerColor = TextBoxColor)
            ) {
                Column(Modifier.padding(12.dp)) {

                    OutlinedTextField(
                        value = routeName,
                        onValueChange = { routeName = it },
                        label = { Text("Route name (e.g., Route A)") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(Modifier.height(10.dp))

                    Text("Points: ${points.size}")

                    Spacer(Modifier.height(10.dp))

                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {

                        Button(
                            onClick = {
                                if (points.isNotEmpty()) {
                                    points = points.dropLast(1)
                                    map?.let { m ->
                                        polyline?.let { m.removePolyline(it) }
                                        polyline = null
                                        if (points.size >= 2) {
                                            polyline = m.addPolyline(PolylineOptions().addAll(points))
                                        }
                                    }
                                }
                            },
                            modifier = Modifier.weight(1f)
                        ) { Text("Undo") }

                        Button(
                            onClick = {
                                points = emptyList()
                                map?.let { m ->
                                    polyline?.let { m.removePolyline(it) }
                                    polyline = null
                                }
                            },
                            modifier = Modifier.weight(1f)
                        ) { Text("Clear") }
                    }

                    Spacer(Modifier.height(10.dp))

                    Button(
                        onClick = {
                            if (routeName.isBlank()) {
                                Toast.makeText(context, "Enter a route name", Toast.LENGTH_SHORT).show()
                                return@Button
                            }
                            if (points.size < 2) {
                                Toast.makeText(context, "Add at least 2 points on map", Toast.LENGTH_SHORT).show()
                                return@Button
                            }

                            val route = RouteModel(
                                name = routeName.trim(),
                                points = points.map { LatLngPoint(lat = it.latitude, lon = it.longitude) }
                            )

                            routeViewModel.saveNewRoute(route) { ok, err ->
                                if (ok) {
                                    Toast.makeText(context, "Route saved!", Toast.LENGTH_SHORT).show()
                                    routeName = ""
                                    points = emptyList()
                                    map?.let { m ->
                                        polyline?.let { m.removePolyline(it) }
                                        polyline = null
                                    }
                                } else {
                                    Toast.makeText(context, err ?: "Failed to save route", Toast.LENGTH_SHORT).show()
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) { Text("Save Route") }
                }
            }
        }
    }
}
