package com.example.cleantrack.view.driver

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.cleantrack.R
import com.example.cleantrack.model.ScheduleModel
import com.example.cleantrack.repository.*
import com.example.cleantrack.ui.theme.*
import com.example.cleantrack.view.common.EditProfileActivity
import com.example.cleantrack.view.common.LogoutDialog
import com.example.cleantrack.view.user.BottomNavItem
import com.example.cleantrack.view.user.ProfileMenuItem
import com.example.cleantrack.view.user.QuickIcon
import com.example.cleantrack.view.user.UserAnnouncementListActivity
import com.example.cleantrack.viewmodel.*
import com.google.android.gms.location.LocationServices
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private lateinit var fusedLocationClient: com.google.android.gms.location.FusedLocationProviderClient

class DriverDashboardActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        enableEdgeToEdge()
        setContent { DriverDashboardBody() }
    }
}

@Composable
fun DriverDashboardBody() {
    val context = LocalContext.current
    var selectedTab by remember { mutableIntStateOf(0) }

    // --- VIEWMODELS ---
    val userViewModel = remember { UserViewModel(UserRepoImpl()) }
    val scheduleViewModel = remember { ScheduleViewModel(ScheduleRepoImpl()) }
    val tripViewModel = remember {
        ActiveTripViewModel(ActiveTripRepoImpl(), UserRepoImpl(), BinRepoImpl(), BinCollectionRepoImpl(), PointsRepoImpl())
    }

    // --- STATE OBSERVERS ---
    val currentUserId = userViewModel.getCurrentUserId() ?: ""
    val currentUser by userViewModel.user.observeAsState()
    val activeTrip by tripViewModel.activeTrip.observeAsState()
    val assignedSchedule by scheduleViewModel.schedule.observeAsState()
    val stats by tripViewModel.binStats.observeAsState(Triple(0, 0, 0))
    val sLoading by scheduleViewModel.loading.observeAsState(false)

    // Used to keep the location callback synced with the latest trip state
    val currentTripState = rememberUpdatedState(activeTrip)

    var showEndTripDialog by remember { mutableStateOf(false) }
    var showLogoutDialog by remember { mutableStateOf(false) }

    val isTripActive = activeTrip?.status == "ACTIVE"

    // --- 1. LOCATION CALLBACK ---
    val locationCallback = remember {
        object : com.google.android.gms.location.LocationCallback() {
            override fun onLocationResult(result: com.google.android.gms.location.LocationResult) {
                val trip = currentTripState.value
                if (trip != null && trip.status == "ACTIVE") {
                    result.lastLocation?.let { location ->
                        tripViewModel.updateLocation(trip.tripId, location.latitude, location.longitude)
                        Log.d("CLEANTRACK", "GPS Lat: ${location.latitude}, Lng: ${location.longitude}")
                    }
                }
            }
        }
    }

    // --- 2. PERMISSIONS ---
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        if (!permissions.entries.all { it.value }) {
            Toast.makeText(context, "Location permission is required for tracking.", Toast.LENGTH_SHORT).show()
        }
    }

    // --- 3. LIFECYCLE EFFECTS ---
    LaunchedEffect(Unit) {
        permissionLauncher.launch(arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION, android.Manifest.permission.ACCESS_COARSE_LOCATION))
    }

    LaunchedEffect(currentUserId) {
        if (currentUserId.isNotEmpty()) {
            userViewModel.getUserById(currentUserId)
            scheduleViewModel.getScheduleByDriver(currentUserId)
        }
    }

    // Sync trip status with the route
    LaunchedEffect(assignedSchedule) {
        assignedSchedule?.routeId?.let { if (it.isNotEmpty()) tripViewModel.observeActiveTripByRoute(it) }
    }

    // Toggle GPS based on Active Status
    LaunchedEffect(activeTrip?.status) {
        if (activeTrip?.status == "ACTIVE") {
            val req = com.google.android.gms.location.LocationRequest.Builder(
                com.google.android.gms.location.Priority.PRIORITY_HIGH_ACCURACY, 5000
            ).setMinUpdateIntervalMillis(2000).build()

            try {
                fusedLocationClient.requestLocationUpdates(req, locationCallback, android.os.Looper.getMainLooper())
            } catch (e: SecurityException) { Log.e("CLEANTRACK", "GPS Security Error", e) }
        } else {
            fusedLocationClient.removeLocationUpdates(locationCallback)
        }
    }

    // --- AUTO-REFRESH/TERMINATE ON TIME EXPIRY ---
    LaunchedEffect(assignedSchedule, activeTrip) {
        assignedSchedule?.let { schedule ->
            if (activeTrip?.status == "ACTIVE") {
                val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
                val currentTime = sdf.format(Date())

                if (currentTime > schedule.endTime) {
                    // This triggers the force-end logic we wrote in the ViewModel
                    tripViewModel.startTripWithValidation(schedule) { _, message ->
                        Toast.makeText(context, "Shift ended: $message", Toast.LENGTH_LONG).show()
                    }
                }
            }
        }
    }

    LaunchedEffect(activeTrip?.status) {
        if (activeTrip?.status == "COMPLETED") {
            assignedSchedule?.routeId?.let { routeId ->
                tripViewModel.observeActiveTripByRoute(routeId)
            }
        }
    }

    DisposableEffect(Unit) { onDispose { fusedLocationClient.removeLocationUpdates(locationCallback) } }

    // --- 4. UI ---
    LogoutDialog(showDialog = showLogoutDialog, onDismiss = { showLogoutDialog = false }, viewModel = userViewModel)

    if (showEndTripDialog) {
        AlertDialog(
            onDismissRequest = { showEndTripDialog = false },
            title = { Text("End Collection?") },
            text = { Text("Do you want to finalize this route and stop tracking?") },
            confirmButton = {
                Button(onClick = {
                    showEndTripDialog = false
                    activeTrip?.let { tripViewModel.endTrip(it.tripId) { _, m -> Toast.makeText(context, m, Toast.LENGTH_SHORT).show() } }
                }, colors = ButtonDefaults.buttonColors(containerColor = Red)) { Text("End Route", color = Color.White) }
            },
            dismissButton = { TextButton(onClick = { showEndTripDialog = false }) { Text("Cancel") } }
        )
    }

    Box(modifier = Modifier.fillMaxSize().background(Brush.verticalGradient(colors = listOf(Blue, Green, Color.White), startY = 0f, endY = 1400f))) {
        Scaffold(
            containerColor = Color.Transparent,
            bottomBar = {
                Surface(modifier = Modifier.fillMaxWidth(), color = Color.White, shadowElevation = 15.dp) {
                    Row(modifier = Modifier.navigationBarsPadding().padding(vertical = 12.dp).fillMaxWidth(), horizontalArrangement = Arrangement.SpaceAround) {
                        BottomNavItem(Icons.Outlined.Home, "Home", active = selectedTab == 0) { selectedTab = 0 }
                        BottomNavItem(Icons.Outlined.Map, "Route", active = selectedTab == 1) {
                            if (isTripActive) context.startActivity(Intent(context, DriverRouteMapActivity::class.java))
                            else Toast.makeText(context, "Start route first", Toast.LENGTH_SHORT).show()
                        }
                        BottomNavItem(Icons.Outlined.PersonOutline, "Profile", active = selectedTab == 2) { selectedTab = 2 }
                    }
                }
            }
        ) { innerPadding ->
            when (selectedTab) {
                0 -> DriverHomeSection(innerPadding, currentUser, activeTrip, assignedSchedule, stats, sLoading, tripViewModel) { showEndTripDialog = true }
                2 -> DriverProfileSection(innerPadding, currentUser, currentUserId) { showLogoutDialog = true }
            }
        }
    }
}
@Composable
fun DriverHomeSection(
    padding: PaddingValues,
    currentUser: com.example.cleantrack.model.UserModel?,
    activeTrip: com.example.cleantrack.model.ActiveTripModel?,
    assignedSchedule: ScheduleModel?,
    stats: Triple<Int, Int, Int>,
    isLoading: Boolean,
    tripVM: ActiveTripViewModel,
    onEndTrip: () -> Unit
) {
    val context = LocalContext.current
    val isTripActive = activeTrip?.status == "ACTIVE"
    val isTripCompleted = activeTrip?.status == "COMPLETED"
    // Check if time is currently expired
    val sdf = remember { SimpleDateFormat("HH:mm", Locale.getDefault()) }
    val currentTime = sdf.format(Date())
    val isTimeExpired = assignedSchedule?.let { currentTime > it.endTime } ?: false

    Column(modifier = Modifier.fillMaxSize().padding(padding).padding(horizontal = 20.dp).verticalScroll(rememberScrollState())) {
        Spacer(modifier = Modifier.height(20.dp))
        Text(text = "Duty Morning ${currentUser?.fullname?.split(" ")?.firstOrNull() ?: "Driver"} 🚛", color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.ExtraBold)

        // Profile Row
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Surface(shape = CircleShape, color = Color.White.copy(0.4f), modifier = Modifier.size(45.dp), border = BorderStroke(1.dp, Color.White.copy(0.5f))) {
                AsyncImage(
                    model = if (!currentUser?.profileImageUrl.isNullOrEmpty()) currentUser?.profileImageUrl else R.drawable.user_logo,
                    contentDescription = null, modifier = Modifier.fillMaxSize().clip(CircleShape), contentScale = ContentScale.Crop
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Text(text = "Status: ${if(isTripActive) "On Route" else "Idle"}", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.weight(1f))
            IconButton(onClick = { context.startActivity(Intent(context, UserAnnouncementListActivity::class.java)) }) {
                Icon(Icons.Outlined.Campaign, null, tint = Color.White, modifier = Modifier.size(28.dp))
            }
        }

        Spacer(modifier = Modifier.height(25.dp))

        // --- PROGRESS CARD ---
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = White),
            elevation = CardDefaults.cardElevation(6.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                val title = when {
                    isTripActive -> activeTrip?.routeName ?: "Active Route"
                    isTripCompleted -> "${activeTrip?.routeName} (Finished)"
                    else -> "No Active Route"
                }
                Text(title, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = if(isTripCompleted) Color.Gray else Black)
                Spacer(modifier = Modifier.height(10.dp))

                val progress = if (stats.first > 0) stats.second.toFloat() / stats.first.toFloat() else 0f
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier.fillMaxWidth().height(8.dp).clip(CircleShape),
                    color = if (isTripCompleted) Color.Gray else Green,
                    trackColor = Color.LightGray.copy(0.3f)
                )
                Spacer(modifier = Modifier.height(12.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Collected: ${stats.second}", color = Green, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Text("Remaining: ${stats.third}", color = Red, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))


        // --- QUICK ACTIONS ---
        Card(shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(containerColor = White), elevation = CardDefaults.cardElevation(6.dp), modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text("Operations", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Spacer(modifier = Modifier.height(20.dp))
                Row(modifier = Modifier.fillMaxWidth(), Arrangement.SpaceAround) {
                    QuickIcon(Icons.Default.QrCodeScanner, "Scan Bin") {
                        if (isTripActive) {
                            val intent = Intent(context, DriverScanBinActivity::class.java).apply {
                                putExtra("TRIP_ID", activeTrip?.tripId)
                                putExtra("ROUTE_ID", activeTrip?.routeId)
                            }
                            context.startActivity(intent)
                        } else Toast.makeText(context, "Start route first", Toast.LENGTH_SHORT).show()
                    }
                    QuickIcon(Icons.Default.Route, "Map", isOutline = true) {
                        if (isTripActive) context.startActivity(Intent(context, DriverRouteMapActivity::class.java))
                        else Toast.makeText(context, "Start route first", Toast.LENGTH_SHORT).show()
                    }
                    QuickIcon(Icons.Default.History, "History", isOutline = true) {
                        context.startActivity(Intent(context, DriversTripHistoryActivity::class.java))
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        if (isLoading) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally), color = White)
        } else if (assignedSchedule != null) {
            RouteDetailCards(schedule = assignedSchedule)
            Spacer(modifier = Modifier.height(20.dp))

            Button(
                onClick = {
                    if (isTripActive) onEndTrip()
                    else {
                        tripVM.startTripWithValidation(assignedSchedule!!) { _, m ->
                            Toast.makeText(context, m, Toast.LENGTH_SHORT).show()
                        }
                    }
                },
                enabled = !isTimeExpired || isTripActive, // Allow stopping if active, but block starting if expired
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(15.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = when {
                        isTripActive -> Red
                        isTimeExpired -> Color.Gray // Gray out if shift is over
                        isTripCompleted -> Blue
                        else -> Green
                    }
                )
            ) {
                Text(
                    text = when {
                        isTripActive -> "Stop Tracking & End Route"
                        isTimeExpired -> "Shift Time Expired"
                        isTripCompleted -> "Resume Today's Route"
                        else -> "Start Collection Route"
                    },
                    fontWeight = FontWeight.Bold, fontSize = 16.sp
                )
            }
        }
    }
}

@Composable
fun DriverProfileSection(padding: PaddingValues, userProfile: com.example.cleantrack.model.UserModel?, uid: String, onLogout: () -> Unit) {
    val context = LocalContext.current
    Column(modifier = Modifier.fillMaxSize().padding(padding).padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Spacer(modifier = Modifier.height(30.dp))
        Surface(shape = CircleShape, color = White.copy(0.3f), modifier = Modifier.size(110.dp), border = BorderStroke(2.dp, White)) {
            AsyncImage(
                model = if (!userProfile?.profileImageUrl.isNullOrEmpty()) userProfile?.profileImageUrl else R.drawable.user_logo,
                contentDescription = null, modifier = Modifier.fillMaxSize().clip(CircleShape), contentScale = ContentScale.Crop
            )
        }
        Spacer(modifier = Modifier.height(15.dp))
        Text(userProfile?.fullname ?: "Driver", color = White, fontSize = 24.sp, fontWeight = FontWeight.ExtraBold)
        Text(userProfile?.email ?: "", color = White.copy(0.8f), fontSize = 15.sp)

        Spacer(modifier = Modifier.height(40.dp))

        Card(shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(containerColor = White), modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(8.dp)) {
                ProfileMenuItem(Icons.Default.Edit, "Edit Profile") {
                    val intent = Intent(context, EditProfileActivity::class.java).apply { putExtra("USER_ID", uid) }
                    context.startActivity(intent)
                }
                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), thickness = 0.5.dp, color = Color.LightGray)
                ProfileMenuItem(Icons.AutoMirrored.Filled.Logout, "Logout", textColor = Red) { onLogout() }
            }
        }
    }
}

@Composable
fun RouteDetailCards(schedule: ScheduleModel) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF1F8E9))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Route, contentDescription = null, tint = Green)
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = schedule.routeName, style = TextStyle(
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                Column {
                    Text("Start Time", fontSize = 12.sp, color = Color.Gray)
                    Text(schedule.startTime, fontWeight = FontWeight.Medium)
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("End Time", fontSize = 12.sp, color = Color.Gray)
                    Text(schedule.endTime, fontWeight = FontWeight.Medium)
                }
            }
        }
    }
}