package com.example.cleantrack.view.user

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.example.cleantrack.R
import com.example.cleantrack.repository.*
import com.example.cleantrack.util.ApiTokenUtil
import com.example.cleantrack.viewmodel.ActiveTripViewModel
import org.maplibre.android.MapLibre
import org.maplibre.android.annotations.IconFactory
import org.maplibre.android.annotations.Marker
import org.maplibre.android.annotations.MarkerOptions
import org.maplibre.android.camera.CameraUpdateFactory
import org.maplibre.android.geometry.LatLng
import org.maplibre.android.maps.MapLibreMap
import org.maplibre.android.maps.MapView
import org.maplibre.android.maps.Style

class UserLiveTrackingActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Initialize MapLibre before setContent
        MapLibre.getInstance(this)

        val routeId = intent.getStringExtra("ROUTE_ID") ?: ""

        setContent {
            UserLiveMapScreen(routeId)
        }
    }
}

@Composable
fun UserLiveMapScreen(routeId: String) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    // Use factory to prevent reconstruction on recomposition
    val vm = remember {
        ActiveTripViewModel(ActiveTripRepoImpl(), UserRepoImpl(), BinRepoImpl(), BinCollectionRepoImpl(),
            PointsRepoImpl())
    }

    val activeTrip by vm.activeTrip.observeAsState()
    val mapView = remember { MapView(context) }
    var mapInstance by remember { mutableStateOf<MapLibreMap?>(null) }
    var truckMarker by remember { mutableStateOf<Marker?>(null) }
    var isStyleLoaded by remember { mutableStateOf(false) }

    val styleUrl = "https://api.baato.io/api/v1/styles/breeze_cdn?key=${ApiTokenUtil.BAATO_API_KEY}"

    // 1. Observe Trip Data
    LaunchedEffect(routeId) {
        vm.observeActiveTripByRoute(routeId)
    }

    // 2. Manage Map Lifecycle (CRITICAL for MapLibre)
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_CREATE -> mapView.onCreate(null)
                Lifecycle.Event.ON_START -> mapView.onStart()
                Lifecycle.Event.ON_RESUME -> mapView.onResume()
                Lifecycle.Event.ON_PAUSE -> mapView.onPause()
                Lifecycle.Event.ON_STOP -> mapView.onStop()
                Lifecycle.Event.ON_DESTROY -> mapView.onDestroy()
                else -> {}
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    // 3. Initialize Map and Style
    LaunchedEffect(mapView) {
        mapView.getMapAsync { m ->
            mapInstance = m
            m.setStyle(styleUrl) {
                isStyleLoaded = true
            }
        }
    }

    // 4. Update Marker when Location changes
    LaunchedEffect(activeTrip?.currentLat, activeTrip?.currentLng, isStyleLoaded) {
        val lat = activeTrip?.currentLat ?: 0.0
        val lng = activeTrip?.currentLng ?: 0.0
        val map = mapInstance

        if (isStyleLoaded && map != null && lat != 0.0 && lng != 0.0) {
            val pos = LatLng(lat, lng)

            if (truckMarker == null) {
                val iconFactory = IconFactory.getInstance(context)
                // Ensure ic_truck exists in res/drawable
                val truckIcon = iconFactory.fromResource(R.drawable.ic_truck)

                truckMarker = map.addMarker(
                    MarkerOptions()
                        .position(pos)
                        .title("Waste Collection Vehicle")
                        .icon(truckIcon)
                )
                // Smoothly zoom to truck on first load
                map.animateCamera(CameraUpdateFactory.newLatLngZoom(pos, 15.0))
            } else {
                // Update existing marker position
                truckMarker?.position = pos
            }
        }
    }

    AndroidView(
        factory = { mapView },
        modifier = Modifier.fillMaxSize()
    )
}