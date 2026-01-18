package com.example.cleantrack.view.admin

import android.app.Activity
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.cleantrack.model.map.LatLngPoint
import com.example.cleantrack.model.map.RouteModel
import com.example.cleantrack.repository.RouteRepoImpl
import com.example.cleantrack.util.ApiTokenUtil
import com.example.cleantrack.viewmodel.RouteViewModel
import org.maplibre.android.MapLibre
import org.maplibre.android.annotations.Polyline
import org.maplibre.android.annotations.PolylineOptions
import org.maplibre.android.camera.CameraPosition
import org.maplibre.android.geometry.LatLng
import org.maplibre.android.maps.MapLibreMap
import org.maplibre.android.maps.MapView

class AdminRouteSetupActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val routeId = intent.getStringExtra("ROUTE_ID")

        setContent {
            AdminRouteSetupScreen(
                savedInstanceState = savedInstanceState,
                routeId = routeId
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminRouteSetupScreen(
    savedInstanceState: Bundle?,
    routeId: String?
) {
    val context = LocalContext.current
    val activity = context as Activity

    MapLibre.getInstance(context.applicationContext)

    val viewModel = remember {
        RouteViewModel(RouteRepoImpl())
    }

    val selectedRoute by viewModel.route.observeAsState()
    val loading by viewModel.loading.observeAsState(false)

    var routeName by remember { mutableStateOf("") }
    var points by remember { mutableStateOf<List<LatLng>>(emptyList()) }
    var baatoApiKey by remember { mutableStateOf<String?>(null) }

    val mapView = remember {
        MapView(context).apply { onCreate(savedInstanceState) }
    }

    var map by remember { mutableStateOf<MapLibreMap?>(null) }
    var polyline by remember { mutableStateOf<Polyline?>(null) }

    /* ---------------- LOAD ROUTE (EDIT MODE) ---------------- */

    LaunchedEffect(routeId) {
        routeId?.let { viewModel.getRouteById(it) }
    }

    LaunchedEffect(Unit) {
        baatoApiKey = ApiTokenUtil.getBaatoApiKey()
    }

    LaunchedEffect(selectedRoute) {
        selectedRoute?.let { route ->
            routeName = route.name
            points = route.points.map { LatLng(it.lat, it.lon) }
        }
    }

    /* ---------------- MAP SETUP ---------------- */

    DisposableEffect(mapView) {
        mapView.getMapAsync { m ->
            map = m
        }

        onDispose {
            mapView.onDestroy()
        }
    }

    LaunchedEffect(map, baatoApiKey) {
        val mapInstance = map
        val apiKey = baatoApiKey
        if (mapInstance == null || apiKey.isNullOrBlank()) {
            return@LaunchedEffect
        }
        val styleUrl = "https://api.baato.io/api/v1/styles/breeze_cdn?key=$apiKey"
        mapInstance.setStyle(styleUrl) {

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

    /* ---------------- SINGLE SOURCE OF TRUTH ---------------- */

    LaunchedEffect(points, map) {
        map?.let { m ->
            polyline?.let { m.removePolyline(it) }
            polyline =
                if (points.size >= 2) {
                    m.addPolyline(PolylineOptions().addAll(points))
                } else null
        }
    }

    /* ---------------- UI ---------------- */

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(if (routeId == null) "Add Route" else "Edit Route")
                }
            )
        }
    ) { pad ->

        if (loading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(pad),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(pad)
            ) {

                // MAP
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                ) {
                    AndroidView(
                        factory = { mapView },
                        modifier = Modifier.fillMaxSize()
                    )
                }

                // CONTROLS
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp)
                ) {
                    Column(Modifier.padding(12.dp)) {

                        OutlinedTextField(
                            value = routeName,
                            onValueChange = { routeName = it },
                            label = { Text("Route Name") },
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(Modifier.height(8.dp))

                        Text("Stops: ${points.size}")

                        Spacer(Modifier.height(8.dp))

                        Row(
                            Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {

                            Button(
                                modifier = Modifier.weight(1f),
                                onClick = {
                                    if (points.isNotEmpty()) {
                                        points = points.dropLast(1)
                                    }
                                }
                            ) {
                                Text("Undo")
                            }

                            Button(
                                modifier = Modifier.weight(1f),
                                onClick = {
                                    points = emptyList()
                                }
                            ) {
                                Text("Clear")
                            }
                        }

                        Spacer(Modifier.height(10.dp))

                        Button(
                            modifier = Modifier.fillMaxWidth(),
                            onClick = {

                                if (routeName.isBlank()) {
                                    Toast.makeText(
                                        context,
                                        "Enter route name",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                    return@Button
                                }

                                if (points.size < 2) {
                                    Toast.makeText(
                                        context,
                                        "Add at least 2 points",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                    return@Button
                                }

                                val model = RouteModel(
                                    routeId = routeId ?: "",
                                    name = routeName.trim(),
                                    points = points.map {
                                        LatLngPoint(
                                            lat = it.latitude,
                                            lon = it.longitude
                                        )
                                    }
                                )

                                if (routeId == null) {
                                    viewModel.addRoute(model) { ok, msg ->
                                        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                                        if (ok) activity.finish()
                                    }
                                } else {
                                    viewModel.updateRoute(model) { ok, msg ->
                                        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                                        if (ok) activity.finish()
                                    }
                                }
                            }
                        ) {
                            Text(if (routeId == null) "Save Route" else "Update Route")
                        }
                    }
                }
            }
        }
    }
}
