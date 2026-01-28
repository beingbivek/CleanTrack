package com.example.cleantrack.view.driver

import android.app.Activity
import android.graphics.Bitmap
import android.graphics.Canvas
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.DepartureBoard
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.example.cleantrack.R
import com.example.cleantrack.repository.*
import com.example.cleantrack.ui.theme.Blue
import com.example.cleantrack.ui.theme.Green
import com.example.cleantrack.util.ApiTokenUtil
import com.example.cleantrack.viewmodel.ActiveTripViewModel
import com.example.cleantrack.viewmodel.RouteViewModel
import com.example.cleantrack.viewmodel.ScheduleViewModel
import com.example.cleantrack.viewmodel.UserViewModel
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
import java.util.*
import kotlin.math.roundToInt

class DriverRouteMapActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        MapLibre.getInstance(this)
        enableEdgeToEdge()
        setContent {
            DriverRouteMapScreen()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DriverRouteMapScreen() {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    // --- VIEWMODELS (Kept Functional as requested) ---
    val userVM = remember { UserViewModel(UserRepoImpl()) }
    val tripVM = remember { ActiveTripViewModel(ActiveTripRepoImpl(), UserRepoImpl(), BinRepoImpl(), BinCollectionRepoImpl(), PointsRepoImpl()) }
    val routeVM = remember { RouteViewModel(RouteRepoImpl()) }
    val scheduleVM = remember { ScheduleViewModel(ScheduleRepoImpl()) }

    // --- STATE ---
    val assignedSchedule by scheduleVM.schedule.observeAsState()
    val activeTrip by tripVM.activeTrip.observeAsState()
    val currentRoute by routeVM.route.observeAsState()

    val mapView = remember { MapView(context).apply { onCreate(null) } }
    var mapInstance by remember { mutableStateOf<MapLibreMap?>(null) }
    var truckMarker by remember { mutableStateOf<Marker?>(null) }
    var currentPolyline by remember { mutableStateOf<Polyline?>(null) }
    var isStyleLoaded by remember { mutableStateOf(false) }
    var baatoApiKey by remember { mutableStateOf<String?>(null) }

    val styleUrl = baatoApiKey?.let { "https://api.baato.io/api/v1/styles/breeze_cdn?key=$it" }

    LaunchedEffect(Unit) {
        baatoApiKey = ApiTokenUtil.getBaatoApiKey()
        userVM.getCurrentUserId()?.let { uid -> scheduleVM.getScheduleByDriver(uid) }
    }

    LaunchedEffect(assignedSchedule) {
        assignedSchedule?.routeId?.let { rid ->
            tripVM.observeActiveTripByRoute(rid)
            routeVM.getRouteById(rid)
        }
    }

    val isTripActiveInSchedule = remember(activeTrip, assignedSchedule) {
        val trip = activeTrip
        val schedule = assignedSchedule
        if (trip == null || schedule == null || trip.status != "ACTIVE") false
        else {
            val today = SimpleDateFormat("EEEE", Locale.getDefault()).format(Date())
            val currentTime = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())
            schedule.dayOfWeek.equals(today, ignoreCase = true) &&
                    currentTime >= schedule.startTime && currentTime <= schedule.endTime
        }
    }

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

    LaunchedEffect(mapView, styleUrl) {
        if (styleUrl != null) {
            mapView.getMapAsync { m ->
                mapInstance = m
                m.setStyle(styleUrl) { isStyleLoaded = true }
            }
        }
    }

    // --- 4. DRAW ROUTE (Green Color Line) ---
    LaunchedEffect(currentRoute, mapInstance, isStyleLoaded) {
        val map = mapInstance
        if (isStyleLoaded && map != null) {
            currentRoute?.let { route ->
                val points = route.points.map { LatLng(it.lat, it.lon) }
                if (points.isNotEmpty()) {
                    currentPolyline?.let { map.removePolyline(it) }
                    currentPolyline = map.addPolyline(
                        PolylineOptions()
                            .addAll(points)
                            .color(android.graphics.Color.parseColor("#4CAF50")) // Clean Green
                            .width(8f)
                    )
                    map.animateCamera(CameraUpdateFactory.newLatLngZoom(points.first(), 13.5))
                }
            }
        }
    }

    // --- 5. UPDATE DRIVER TRUCK MARKER (Blue Icon) ---
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
                    DrawableCompat.setTint(wrapped, android.graphics.Color.parseColor("#2196F3"))
                    val sizePx = (40 * context.resources.displayMetrics.density).roundToInt()
                    val bitmap = Bitmap.createBitmap(sizePx, sizePx, Bitmap.Config.ARGB_8888)
                    val canvas = Canvas(bitmap)
                    wrapped.setBounds(0, 0, sizePx, sizePx)
                    wrapped.draw(canvas)
                    iconFactory.fromBitmap(bitmap)
                } ?: iconFactory.fromResource(R.drawable.ic_truck)

                truckMarker = map.addMarker(MarkerOptions().position(pos).title("Your Location").icon(truckIcon))
                map.animateCamera(CameraUpdateFactory.newLatLngZoom(pos, 15.0))
            } else {
                truckMarker?.position = pos
            }
        } else if (!isTripActiveInSchedule) {
            truckMarker?.let { mapInstance?.removeMarker(it) }
            truckMarker = null
        }
    }

    // --- UI STRUCTURE ---
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(Blue, Green, Color.White),
                    startY = 0f,
                    endY = 1000f
                )
            )
    ) {
        Scaffold(
            containerColor = Color.Transparent, // Critical for gradient visibility
            topBar = {
                CenterAlignedTopAppBar(
                    title = {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Active Route Map", fontWeight = FontWeight.ExtraBold, color = Color.White, fontSize = 18.sp)
                            assignedSchedule?.let {
                                Text(it.routeName, fontSize = 11.sp, color = Color.White.copy(0.8f), fontWeight = FontWeight.Bold)
                            }
                        }
                    },
                    navigationIcon = {
                        IconButton(onClick = { (context as? Activity)?.finish() }) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                        }
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.Transparent)
                )
            }
        ) { pad ->
            Box(modifier = Modifier.fillMaxSize().padding(pad)) {

                // Map Container with Rounded Corners
                Card(
                    modifier = Modifier.fillMaxSize().padding(16.dp),
                    shape = RoundedCornerShape(24.dp),
                    elevation = CardDefaults.cardElevation(8.dp)
                ) {
                    AndroidView(factory = { mapView }, modifier = Modifier.fillMaxSize())
                }

                // Inactive Overlay (Improved Clean Track Styling)
                if (!isTripActiveInSchedule) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = 0.5f)) // Dim the background
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Card(
                            shape = RoundedCornerShape(24.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            elevation = CardDefaults.cardElevation(10.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(32.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Box(
                                    modifier = Modifier.size(80.dp).background(Blue.copy(0.1f), RoundedCornerShape(20.dp)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Default.DepartureBoard, null, tint = Blue, modifier = Modifier.size(45.dp))
                                }
                                Spacer(Modifier.height(20.dp))
                                Text(
                                    "No Active Shift",
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color.DarkGray
                                )
                                Spacer(Modifier.height(8.dp))
                                Text(
                                    "Tracking is inactive. Please start your route from the Driver Dashboard to see your live progress and collection points.",
                                    textAlign = TextAlign.Center,
                                    fontSize = 14.sp,
                                    color = Color.Gray,
                                    lineHeight = 20.sp
                                )
                                Spacer(Modifier.height(24.dp))
                                Button(
                                    onClick = { (context as? Activity)?.finish() },
                                    colors = ButtonDefaults.buttonColors(containerColor = Blue),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Text("Go to Dashboard", fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                } else {
                    // Floating Badge for Route Status when active
                    Surface(
                        modifier = Modifier.align(Alignment.TopEnd).padding(32.dp),
                        color = Color.White,
                        shape = RoundedCornerShape(12.dp),
                        shadowElevation = 4.dp
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Navigation, null, tint = Green, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("Live Tracking", color = Green, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }
                }
            }
        }
    }
}