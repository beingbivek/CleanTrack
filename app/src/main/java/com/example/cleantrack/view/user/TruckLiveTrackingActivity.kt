package com.example.cleantrack.view.user

import android.os.Bundle
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DepartureBoard
import androidx.compose.material.icons.filled.Email
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.example.cleantrack.R
import com.example.cleantrack.repository.*
import com.example.cleantrack.ui.theme.Green
import com.example.cleantrack.util.ApiTokenUtil
import com.example.cleantrack.viewmodel.ActiveTripViewModel
import com.example.cleantrack.viewmodel.RouteViewModel
import com.example.cleantrack.viewmodel.ScheduleViewModel
import org.maplibre.android.MapLibre
import org.maplibre.android.annotations.IconFactory
import org.maplibre.android.annotations.Marker
import org.maplibre.android.annotations.MarkerOptions
import org.maplibre.android.annotations.Polyline
import org.maplibre.android.annotations.PolylineOptions
import org.maplibre.android.camera.CameraUpdateFactory
import org.maplibre.android.geometry.LatLng
import org.maplibre.android.maps.MapLibreMap
import org.maplibre.android.maps.MapView
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.roundToInt

class TruckLiveTrackingActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Initialize MapLibre before setContent
        MapLibre.getInstance(this)

        val routeId = intent.getStringExtra("ROUTE_ID") ?: ""

        setContent {
            TruckLiveMapScreen(routeId)
        }
    }
}

@Composable
fun TruckLiveMapScreen(routeId: String) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    // Use factory to prevent reconstruction on recomposition
    val vm = remember {
        ActiveTripViewModel(ActiveTripRepoImpl(), UserRepoImpl(), BinRepoImpl(), BinCollectionRepoImpl(),
            PointsRepoImpl())
    }
    val routeVM = remember { RouteViewModel(RouteRepoImpl()) }
    val scheduleVM = remember { ScheduleViewModel(ScheduleRepoImpl()) }

    val activeTrip by vm.activeTrip.observeAsState()
    val currentRoute by routeVM.route.observeAsState()
    val currentSchedule by scheduleVM.schedule.observeAsState()

    val mapView = remember { MapView(context).apply { onCreate(null) } }
    var mapInstance by remember { mutableStateOf<MapLibreMap?>(null) }
    var truckMarker by remember { mutableStateOf<Marker?>(null) }
    var currentPolyline by remember { mutableStateOf<Polyline?>(null) }
    var isStyleLoaded by remember { mutableStateOf(false) }

    val styleUrl = "https://api.baato.io/api/v1/styles/breeze_cdn?key=${ApiTokenUtil.BAATO_API_KEY}"

    // 1. Observe Trip Data
    LaunchedEffect(routeId) {
        vm.observeActiveTripByRoute(routeId)
    }

    LaunchedEffect(activeTrip?.scheduleId) {
        val scheduleId = activeTrip?.scheduleId.orEmpty()
        if (scheduleId.isNotBlank()) {
            scheduleVM.getScheduleById(scheduleId)
        }
    }

    LaunchedEffect(activeTrip?.routeId, routeId) {
        val resolvedRouteId = activeTrip?.routeId?.ifBlank { null } ?: routeId
        if (resolvedRouteId.isNotBlank()) {
            routeVM.getRouteById(resolvedRouteId)
        }
    }

    val isTripActiveInSchedule = remember(activeTrip, currentSchedule) {
        val trip = activeTrip
        val schedule = currentSchedule
        if (trip == null || schedule == null) {
            false
        } else if (trip.status != "ACTIVE") {
            false
        } else {
            val dayFormat = SimpleDateFormat("EEEE", Locale.getDefault())
            val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
            val today = dayFormat.format(Date())
            val currentTime = timeFormat.format(Date())
            schedule.dayOfWeek.equals(today, ignoreCase = true) &&
                    currentTime >= schedule.startTime &&
                    currentTime <= schedule.endTime
        }
    }

    // 2. Manage Map Lifecycle (CRITICAL for MapLibre)
    DisposableEffect(lifecycleOwner, mapView) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_START -> mapView.onStart()
                Lifecycle.Event.ON_RESUME -> mapView.onResume()
                Lifecycle.Event.ON_PAUSE -> mapView.onPause()
                Lifecycle.Event.ON_STOP -> mapView.onStop()
                else -> {}
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            mapView.onDestroy()
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

    // 4. Update Route + Marker when Location changes
    LaunchedEffect(currentRoute, mapInstance, isStyleLoaded, isTripActiveInSchedule) {
        val map = mapInstance
        if (isTripActiveInSchedule && isStyleLoaded && map != null) {
            currentRoute?.let { route ->
                val points = route.points.map { LatLng(it.lat, it.lon) }
                if (points.isNotEmpty()) {
                    currentPolyline?.let { map.removePolyline(it) }
                    currentPolyline = map.addPolyline(
                        PolylineOptions()
                            .addAll(points)
                            .color(android.graphics.Color.parseColor("#4CAF50"))
                            .width(6f)
                    )
                    map.animateCamera(CameraUpdateFactory.newLatLngZoom(points.first(), 13.0))
                }
            }
        } else {
            currentPolyline?.let { map?.removePolyline(it) }
            currentPolyline = null
        }
    }

    LaunchedEffect(activeTrip?.currentLat, activeTrip?.currentLng, isStyleLoaded, isTripActiveInSchedule) {
        val lat = activeTrip?.currentLat ?: 0.0
        val lng = activeTrip?.currentLng ?: 0.0
        val map = mapInstance

        if (isTripActiveInSchedule && isStyleLoaded && map != null && lat != 0.0 && lng != 0.0) {
            val pos = LatLng(lat, lng)

            if (truckMarker == null) {
                val iconFactory = IconFactory.getInstance(context)
                val truckDrawable = ContextCompat.getDrawable(context, R.drawable.ic_truck)?.mutate()
                val truckIcon = truckDrawable?.let { drawable ->
                    val wrapped = DrawableCompat.wrap(drawable)
                    DrawableCompat.setTint(wrapped, Color.parseColor("#4CAF50"))
                    val sizePx = (32 * context.resources.displayMetrics.density).roundToInt()
                    val bitmap = Bitmap.createBitmap(sizePx, sizePx, Bitmap.Config.ARGB_8888)
                    val canvas = Canvas(bitmap)
                    wrapped.setBounds(0, 0, sizePx, sizePx)
                    wrapped.draw(canvas)
                    iconFactory.fromBitmap(bitmap)
                } ?: iconFactory.fromResource(R.drawable.ic_truck)

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
        } else {
            truckMarker?.let { map?.removeMarker(it) }
            truckMarker = null
        }
    }

    if (isTripActiveInSchedule) {
        AndroidView(
            factory = { mapView },
            modifier = Modifier.fillMaxSize()
        )
    } else {
        Column(
            modifier = Modifier.fillMaxSize().background(color = MaterialTheme.colorScheme.background).padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,

            ) {
            Icon(imageVector = Icons.Default.DepartureBoard, contentDescription = "Not Available", tint = Green)

            Spacer(Modifier.height(5.dp))

            Text(
                text = "Live tracking is available only during the active route schedule.",
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(10.dp)
            )
        }
    }
}
