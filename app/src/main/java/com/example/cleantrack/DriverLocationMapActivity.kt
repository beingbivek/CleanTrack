package com.example.cleantrack

import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.app.ActivityCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.example.cleantrack.ui.theme.*
import com.example.cleantrack.util.ApiTokenUtil
import com.google.android.gms.location.*
import org.maplibre.android.MapLibre
import org.maplibre.android.annotations.Marker
import org.maplibre.android.annotations.MarkerOptions
import org.maplibre.android.camera.CameraPosition
import org.maplibre.android.geometry.LatLng
import org.maplibre.android.maps.MapLibreMap
import org.maplibre.android.maps.MapView

private var driverMapViewState: MapView? = null

class DriverLocationMapActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        MapLibre.getInstance(applicationContext)
        setContent { DriverMapComposable(savedInstanceState) }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        driverMapViewState?.onSaveInstanceState(outState)
    }

    override fun onLowMemory() {
        super.onLowMemory()
        driverMapViewState?.onLowMemory()
    }
}

// Check if location permission granted
fun hasDriverLocationPermission(context: Context): Boolean {
    return ActivityCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
            ActivityCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
}

@SuppressLint("MissingPermission")
@Composable
fun DriverMapComposable(savedInstanceState: Bundle?) {
    val context = LocalContext.current

    var currentLat by remember { mutableStateOf(27.7172) }
    var currentLon by remember { mutableStateOf(85.3240) }

    val mapView = remember { MapView(context).apply { onCreate(savedInstanceState) } }
    val styleUrl = "https://api.baato.io/api/v1/styles/breeze_cdn?key=${ApiTokenUtil.BAATO_API_KEY}"

    var mapInstance by remember { mutableStateOf<MapLibreMap?>(null) }
    var markerInstance by remember { mutableStateOf<Marker?>(null) }
    var hasCenteredOnGPS by remember { mutableStateOf(false) }

    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }
    val locationRequest = remember { LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 2000).build() }

    val locationCallback = remember {
        object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                val loc = result.lastLocation ?: return
                val pos = LatLng(loc.latitude, loc.longitude)

                // Add marker first time, else update
                if (markerInstance == null && mapInstance != null) {
                    markerInstance = mapInstance?.addMarker(
                        MarkerOptions().position(pos).title("Driver Location")
                    )
                } else {
                    markerInstance?.position = pos
                }

                // Center map on first location
                if (!hasCenteredOnGPS && mapInstance != null) {
                    hasCenteredOnGPS = true
                    mapInstance?.animateCamera(
                        org.maplibre.android.camera.CameraUpdateFactory.newLatLngZoom(pos, 15.0)
                    )
                }

                currentLat = loc.latitude
                currentLon = loc.longitude
            }
        }
    }

    // Permission Launcher
    val locationPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { perms ->
        if (perms[android.Manifest.permission.ACCESS_FINE_LOCATION] == true ||
            perms[android.Manifest.permission.ACCESS_COARSE_LOCATION] == true
        ) {
            fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null)
        } else {
            Toast.makeText(context, "Location permission denied", Toast.LENGTH_SHORT).show()
        }
    }

    LaunchedEffect(Unit) {
        if (!hasDriverLocationPermission(context)) {
            locationPermissionLauncher.launch(
                arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION, android.Manifest.permission.ACCESS_COARSE_LOCATION)
            )
        } else {
            fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null)
        }
    }

    // Initialize map
    DisposableEffect(mapView) {
        mapView.getMapAsync { map ->
            mapInstance = map
            map.setStyle(styleUrl) {
                val pos = LatLng(currentLat, currentLon)
                map.cameraPosition = CameraPosition.Builder().target(pos).zoom(12.0).build()
            }
        }
        onDispose {}
    }

    // Map lifecycle
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner, mapView) {
        driverMapViewState = mapView
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_START -> mapView.onStart()
                Lifecycle.Event.ON_RESUME -> mapView.onResume()
                Lifecycle.Event.ON_PAUSE -> mapView.onPause()
                Lifecycle.Event.ON_STOP -> mapView.onStop()
                Lifecycle.Event.ON_DESTROY -> {
                    fusedLocationClient.removeLocationUpdates(locationCallback)
                    mapView.onDestroy()
                }
                else -> {}
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            driverMapViewState = null
            lifecycleOwner.lifecycle.removeObserver(observer)
            fusedLocationClient.removeLocationUpdates(locationCallback)
            mapView.onDestroy()
        }
    }

    // UI
    Scaffold { pad ->
        Column(modifier = Modifier.fillMaxSize().padding(pad)) {
            Box(modifier = Modifier.fillMaxWidth().weight(1f)) {
                AndroidView(factory = { mapView }, modifier = Modifier.fillMaxSize())
            }

            Card(
                modifier = Modifier.fillMaxWidth().padding(8.dp),
                colors = CardDefaults.cardColors(containerColor = TextBoxColor),
                elevation = CardDefaults.cardElevation(4.dp)
            ) {
                Column(Modifier.padding(12.dp)) {
                    Text("Driver Live Location:", style = MaterialTheme.typography.titleMedium)
                    Text(
                        "Lat: ${String.format("%.6f", currentLat)}, Lon: ${String.format("%.6f", currentLon)}",
                        style = MaterialTheme.typography.headlineSmall,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
        }
    }
}
