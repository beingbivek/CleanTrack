package com.example.cleantrack.view.user

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import com.example.cleantrack.util.ApiTokenUtil
import com.example.cleantrack.viewmodel.ActiveTripViewModel
import com.example.cleantrack.repository.ActiveTripRepoImpl
import org.maplibre.android.MapLibre
import org.maplibre.android.geometry.LatLng
import org.maplibre.android.maps.MapView
import org.maplibre.android.maps.MapLibreMap
import org.maplibre.android.annotations.Marker
import org.maplibre.android.annotations.MarkerOptions

class UserLiveTrackingActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        MapLibre.getInstance(applicationContext)

        // 🔹 user’s assigned route
        val routeId = intent.getStringExtra("ROUTE_ID") ?: return

        setContent {
            UserLiveMapScreen(routeId)
        }
    }
}

@Composable
fun UserLiveMapScreen(routeId: String) {

    val context = LocalContext.current
    val vm = remember { ActiveTripViewModel(ActiveTripRepoImpl()) }

    val activeTrip by vm.activeTrip.observeAsState()

    val mapView = remember { MapView(context).apply { onCreate(null) } }
    var map by remember { mutableStateOf<MapLibreMap?>(null) }
    var marker by remember { mutableStateOf<Marker?>(null) }

    val styleUrl =
        "https://api.baato.io/api/v1/styles/breeze_cdn?key=${ApiTokenUtil.BAATO_API_KEY}"

    // 🔴 Start listening
    LaunchedEffect(routeId) {
        vm.observeActiveTripByRoute(routeId)
    }

    DisposableEffect(mapView) {
        mapView.getMapAsync { m ->
            map = m
            m.setStyle(styleUrl)
        }
        onDispose { mapView.onDestroy() }
    }

    // 🔄 Update marker when driver moves
    LaunchedEffect(activeTrip?.currentLat, activeTrip?.currentLng) {

        val lat = activeTrip?.currentLat
        val lng = activeTrip?.currentLng

        if (lat != null && lng != null && map != null) {

            val pos = LatLng(lat, lng)

            if (marker == null) {
                marker = map!!.addMarker(
                    MarkerOptions().position(pos).title("Waste Collection Vehicle")
                )
                map!!.moveCamera(
                    org.maplibre.android.camera.CameraUpdateFactory.newLatLngZoom(pos, 15.0)
                )
            } else {
                marker!!.position = pos
            }
        }
    }

    AndroidView(factory = { mapView }, modifier = Modifier.fillMaxSize())
}
