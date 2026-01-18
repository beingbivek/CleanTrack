package com.example.cleantrack.view.driver

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.app.ActivityCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.example.cleantrack.model.BinCollectionModel
import com.example.cleantrack.model.BinModel
import com.example.cleantrack.viewmodel.ActiveTripViewModel
import com.example.cleantrack.repository.ActiveTripRepoImpl
import com.example.cleantrack.repository.BinCollectionRepoImpl
import com.example.cleantrack.repository.BinRepoImpl
import com.example.cleantrack.repository.PointsRepoImpl
import com.example.cleantrack.repository.UserRepoImpl
import com.example.cleantrack.ui.theme.TextBoxColor
import com.example.cleantrack.util.ApiTokenUtil
import com.journeyapps.barcodescanner.ScanContract
import com.journeyapps.barcodescanner.ScanOptions
import com.google.android.gms.location.*

import org.maplibre.android.MapLibre
import org.maplibre.android.maps.MapView
import org.maplibre.android.maps.MapLibreMap
import org.maplibre.android.annotations.Marker
import org.maplibre.android.annotations.MarkerOptions
import org.maplibre.android.camera.CameraPosition
import org.maplibre.android.camera.CameraUpdateFactory
import org.maplibre.android.geometry.LatLng

class DriverLocationMapActivity : ComponentActivity() {
    private lateinit var activeTripViewModel: ActiveTripViewModel
    private var tripId: String = ""
    private var driverMapViewState: MapView? = null

    private val scanLauncher =
        registerForActivityResult(ScanContract()) { result ->
            if (result.contents != null) {
                val scannedBinId = result.contents
                validateBin(scannedBinId)
            }
        }

    private fun validateBin(binId: String) {
        activeTripViewModel.getBinById(binId) { success, message, bin ->
            if (success && bin != null) {
                rateBin(bin)
            } else {
                Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun rateBin(bin: BinModel) {
        val rating = 0
        val remarks = "Good segregation"
        val segregatedCorrectly = true
        val pointsAwarded = 1

        val collectionModel = BinCollectionModel(
            binId = bin.binId,
            driverId = "Driver ID",
            userId = bin.ownerUserId,
            tripId = tripId,
            rating = rating,
            remarks = remarks,
            segregatedCorrectly = segregatedCorrectly,
            pointsAwarded = pointsAwarded
        )

        activeTripViewModel.addBinCollection(collectionModel) { success, message ->
            if (success) {
                Toast.makeText(this, "Bin rated successfully", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        MapLibre.getInstance(applicationContext)
        tripId = intent.getStringExtra("TRIP_ID") ?: ""

        activeTripViewModel = ActiveTripViewModel(ActiveTripRepoImpl(), UserRepoImpl(), BinRepoImpl(),
            BinCollectionRepoImpl(), PointsRepoImpl()
        )

        setContent {
            DriverMapComposable(
                savedInstanceState = savedInstanceState,
                tripId = tripId,
                activeTripViewModel = activeTripViewModel
            )
        }
    }

    fun hasDriverLocationPermission(context: Context): Boolean {
        return ActivityCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }

    // FIX: Added Annotation here to tell Lint we are handling permissions
    @SuppressLint("MissingPermission")
    @Composable
    fun DriverMapComposable(
        savedInstanceState: Bundle?,
        tripId: String,
        activeTripViewModel: ActiveTripViewModel
    ) {
        val context = LocalContext.current

        var currentLat by remember { mutableStateOf(27.7172) }
        var currentLon by remember { mutableStateOf(85.3240) }
        var baatoApiKey by remember { mutableStateOf<String?>(null) }

        val mapView = remember { MapView(context).apply { onCreate(savedInstanceState) } }

        var mapInstance by remember { mutableStateOf<MapLibreMap?>(null) }
        var markerInstance by remember { mutableStateOf<Marker?>(null) }
        var hasCenteredOnGPS by remember { mutableStateOf(false) }

        val fusedLocationClient =
            remember { LocationServices.getFusedLocationProviderClient(context) }

        val locationRequest = remember {
            LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 2000).build()
        }

        val locationCallback = remember {
            object : LocationCallback() {
                override fun onLocationResult(result: LocationResult) {
                    val loc = result.lastLocation ?: return
                    val pos = LatLng(loc.latitude, loc.longitude)

                    if (tripId.isNotBlank()) {
                        activeTripViewModel.updateLocation(
                            tripId,
                            loc.latitude,
                            loc.longitude
                        )
                    }

                    if (markerInstance == null && mapInstance != null) {
                        markerInstance = mapInstance?.addMarker(
                            MarkerOptions().position(pos).title("Driver Location")
                        )
                    } else {
                        markerInstance?.position = pos
                    }

                    if (!hasCenteredOnGPS && mapInstance != null) {
                        hasCenteredOnGPS = true
                        mapInstance?.animateCamera(
                            CameraUpdateFactory.newLatLngZoom(pos, 15.0)
                        )
                    }

                    currentLat = loc.latitude
                    currentLon = loc.longitude
                }
            }
        }

        val locationPermissionLauncher = rememberLauncherForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { permissions ->
            val permissionGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                    permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true

            if (permissionGranted) {
                // FIX: Wrapped in try-catch for safety
                try {
                    fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null)
                } catch (e: SecurityException) {
                    e.printStackTrace()
                }
            } else {
                Toast.makeText(context, "Location permission denied", Toast.LENGTH_SHORT).show()
            }
        }

        LaunchedEffect(Unit) {
            if (!hasDriverLocationPermission(context)) {
                locationPermissionLauncher.launch(
                    arrayOf(
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    )
                )
            } else {
                // FIX: Wrapped in try-catch for safety
                try {
                    fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null)
                } catch (e: SecurityException) {
                    e.printStackTrace()
                }
            }
        }

        LaunchedEffect(Unit) {
            baatoApiKey = ApiTokenUtil.getBaatoApiKey()
        }

        DisposableEffect(mapView) {
            mapView.getMapAsync { map ->
                mapInstance = map
            }
            onDispose {}
        }

        LaunchedEffect(mapInstance, baatoApiKey) {
            val map = mapInstance
            val apiKey = baatoApiKey
            if (map == null || apiKey.isNullOrBlank()) {
                return@LaunchedEffect
            }
            val styleUrl = "https://api.baato.io/api/v1/styles/breeze_cdn?key=$apiKey"
            map.setStyle(styleUrl) {
                val pos = LatLng(currentLat, currentLon)
                map.cameraPosition =
                    CameraPosition.Builder().target(pos).zoom(12.0).build()
            }
        }

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

        Scaffold { pad ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(pad)
            ) {
                Box(modifier = Modifier.fillMaxWidth().weight(1f)) {
                    AndroidView(factory = { mapView }, modifier = Modifier.fillMaxSize())
                }

                Button(onClick = { scanLauncher.launch(ScanOptions()) }) {
                    Text("Scan Bin QR")
                }

                Card(
                    modifier = Modifier.fillMaxWidth().padding(8.dp),
                    colors = CardDefaults.cardColors(containerColor = TextBoxColor)
                ) {
                    Column(Modifier.padding(12.dp)) {
                        Text("Driver Live Location", style = MaterialTheme.typography.titleMedium)
                        Text(
                            "Lat: ${String.format("%.6f", currentLat)}, Lon: ${String.format("%.6f", currentLon)}"
                        )
                    }
                }
            }
        }
    }
}
