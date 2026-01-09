package com.example.cleantrack.view.driver

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.cleantrack.model.AnnouncementModel
import com.example.cleantrack.model.ScheduleModel
import com.example.cleantrack.repository.*
import com.example.cleantrack.ui.theme.*
import com.example.cleantrack.view.common.AnnouncementBanner
import com.example.cleantrack.view.common.EditProfileActivity
import com.example.cleantrack.view.common.LogoutDialog
import com.example.cleantrack.view.user.UserAnnouncementListActivity
import com.example.cleantrack.view.user.UserScheduleListActivity
import com.example.cleantrack.viewmodel.*
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices

private lateinit var fusedLocationClient: FusedLocationProviderClient

class DemoDriverDashActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        enableEdgeToEdge()
        setContent {
            DriverDashboardScreens()
        }
    }
}
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DriverDashboardScreens() {
    val context = LocalContext.current
    var selectedTab by remember { mutableIntStateOf(0) }





    // --- FUNCTIONAL INITIALIZATION ---
    val userViewModel = remember { UserViewModel(UserRepoImpl()) }
    val scheduleViewModel = remember { ScheduleViewModel(ScheduleRepoImpl()) }
    val announcementVM = remember { AnnouncementViewModel(AnnouncementRepoImpl()) }
    val tripViewModel = remember {
        ActiveTripViewModel(ActiveTripRepoImpl(), UserRepoImpl(), BinRepoImpl(), BinCollectionRepoImpl())
    }


    val sLoading by scheduleViewModel.loading.observeAsState(false)

    val currentUserId = userViewModel.getCurrentUserId() ?: ""
    val currentUser by userViewModel.user!!.observeAsState()
    val activeTrip by tripViewModel.activeTrip.observeAsState()
    val assignedSchedule by scheduleViewModel.schedule.observeAsState(null)
    val stats by tripViewModel.binStats.observeAsState(Triple(0, 0, 0))

    var showEndTripDialog by remember { mutableStateOf(false) }
    var showLogoutDialog by remember { mutableStateOf(false) }

    // --- LOCATION LOGIC ---
    val locationCallback = remember {
        object : com.google.android.gms.location.LocationCallback() {
            override fun onLocationResult(result: com.google.android.gms.location.LocationResult) {
                result.lastLocation?.let { location ->
                    activeTrip?.let { trip ->
                        tripViewModel.updateLocation(trip.tripId, location.latitude, location.longitude)
                    }
                }
            }
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        if (!permissions.entries.all { it.value }) {
            Toast.makeText(context, "Location permission required.", Toast.LENGTH_LONG).show()
        }
    }

    LaunchedEffect(Unit) {
        permissionLauncher.launch(arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION, android.Manifest.permission.ACCESS_COARSE_LOCATION))
    }

    LaunchedEffect(currentUserId) {
        if (currentUserId.isNotEmpty()) {
            userViewModel.getUserById(currentUserId)
            announcementVM.getAllAnnouncements { _, _, _ -> }
            scheduleViewModel.getScheduleByDriver(currentUserId)
        }
    }

    LaunchedEffect(assignedSchedule) {
        assignedSchedule?.routeId?.let { if (it.isNotEmpty()) tripViewModel.observeActiveTripByRoute(it) }
    }

    LaunchedEffect(activeTrip) {
        if (activeTrip != null && activeTrip?.status == "ACTIVE") {
            val req = com.google.android.gms.location.LocationRequest.Builder(com.google.android.gms.location.Priority.PRIORITY_HIGH_ACCURACY, 2000).build()
            try { fusedLocationClient.requestLocationUpdates(req, locationCallback, android.os.Looper.getMainLooper()) }
            catch (e: SecurityException) { Log.e("CLEANTRACK", "GPS Error", e) }
        } else {
            fusedLocationClient.removeLocationUpdates(locationCallback)
        }
    }

    DisposableEffect(Unit) { onDispose { fusedLocationClient.removeLocationUpdates(locationCallback) } }

    LogoutDialog(showDialog = showLogoutDialog, onDismiss = { showLogoutDialog = false }, viewModel = userViewModel)

    if (showEndTripDialog) {
        AlertDialog(
            onDismissRequest = { showEndTripDialog = false },
            title = { Text("End Route?") },
            text = { Text("Are you sure you want to end the collection route?") },
            confirmButton = {
                Button(onClick = {
                    showEndTripDialog = false
                    activeTrip?.let { tripViewModel.endTrip(it.tripId) { s, m -> Toast.makeText(context, m, Toast.LENGTH_SHORT).show() } }
                }, colors = ButtonDefaults.buttonColors(containerColor = Red)) { Text("End Route", color = Color.White) }
            },
            dismissButton = { TextButton(onClick = { showEndTripDialog = false }) { Text("Cancel") } }
        )
    }

    Scaffold(
        bottomBar = {
            NavigationBar(
                containerColor = Color.White,
                tonalElevation = 8.dp,
                modifier = Modifier.clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
            ) {
                NavigationBarItem(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    icon = { Icon(Icons.Default.Home, null) },
                    label = { Text("Home") },
                    colors = NavigationBarItemDefaults.colors(selectedIconColor = Blue, indicatorColor = TextBoxColor)
                )
                NavigationBarItem(
                    selected = selectedTab == 1,
                    onClick = {
                        if (activeTrip != null && activeTrip?.status == "ACTIVE") {
                            selectedTab = 1
                            context.startActivity(Intent(context, DriverRouteMapActivity::class.java))
                        } else {
                            Toast.makeText(context, "Please start the Collection Route to view the map", Toast.LENGTH_SHORT).show()
                        }
                    },
                    icon = { Icon(Icons.Default.Map, null) },
                    label = { Text("Route") },
                    colors = NavigationBarItemDefaults.colors(selectedIconColor = Blue, indicatorColor = TextBoxColor)
                )
                NavigationBarItem(
                    selected = selectedTab == 2,
                    onClick = {
                        val intent = Intent(context, EditProfileActivity::class.java)
                        intent.putExtra("USER_ID", currentUserId)
                        context.startActivity(intent)
                    },
                    icon = { Icon(Icons.Default.AccountCircle, null) },
                    label = { Text("Profile") },
                    colors = NavigationBarItemDefaults.colors(selectedIconColor = Blue, indicatorColor = TextBoxColor)
                )
            }
        }
    ) { innerPadding ->
        Box(modifier = Modifier.fillMaxSize().padding(innerPadding).background(Color(0xFFF8FAF9))) {

            // 1. Header with Gradient (Matches Login Button Gradient)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(220.dp)
                    .background(
                        Brush.horizontalGradient(colors = ButtonColor),
                        shape = RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp)
                    )
                    .padding(horizontal = 24.dp, vertical = 30.dp)
            ) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Column {
                        Text(text = "Driver Dashboard", style = TextStyle(color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Bold))
                        Text(text = "Welcome, ${currentUser?.fullname ?: "Driver"} 👋", style = TextStyle(color = Color.White.copy(alpha = 0.9f), fontSize = 14.sp))
                    }
                    IconButton(onClick = { showLogoutDialog = true }) {
                        Icon(Icons.Default.PowerSettingsNew, contentDescription = "Logout", tint = Color.White)
                    }
                }
            }

            // 2. Scrollable Body
            Column(modifier = Modifier.fillMaxSize().padding(top = 110.dp).padding(horizontal = 20.dp)) {

                // Progress Card
                Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(24.dp), colors = CardDefaults.cardColors(containerColor = Color.White), elevation = CardDefaults.cardElevation(6.dp)) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text(activeTrip?.routeName ?: "No Active Route", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        Spacer(modifier = Modifier.height(12.dp))

                        val progress = if (stats.first > 0) stats.second.toFloat() / stats.first.toFloat() else 0f
                        LinearProgressIndicator(
                            progress = progress,
                            modifier = Modifier.fillMaxWidth().height(8.dp).clip(CircleShape),
                            color = Green,
                            trackColor = TextBoxColor
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Collected: ${stats.second}", fontSize = 14.sp, color = Green, fontWeight = FontWeight.Bold)
                            Text("Remains: ${stats.third}", fontSize = 14.sp, color = Red, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Text("Quick Actions", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                Spacer(modifier = Modifier.height(16.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    QuickActionItem(Icons.Default.QrCodeScanner, "Scan Bin") {
                        // 1. Check if there is an active trip
                        val currentTrip = activeTrip

                        if (currentTrip != null && currentTrip.status == "ACTIVE") {
                            // 2. Pass the tripId to the Scanner Activity
                            val intent = Intent(context, DriverScanBinActivity::class.java).apply {
                                putExtra("TRIP_ID", currentTrip.tripId)
                            }
                            context.startActivity(intent)
                        } else {
                            // 3. Alert the driver if they haven't started the route yet
                            Toast.makeText(context, "Please start a route before scanning bins", Toast.LENGTH_SHORT).show()
                        }
                    }
                    QuickActionItem(Icons.Default.Map, "Route Map") {
                        if (activeTrip != null && activeTrip?.status == "ACTIVE") {
                            context.startActivity(Intent(context, DriverRouteMapActivity::class.java))
                        } else {
                            Toast.makeText(context, "Access Denied: Start your route first", Toast.LENGTH_SHORT).show()
                        }
                    }
                    QuickActionItem(Icons.Default.History, "Recent") { /* Logs Activity */ }
                    QuickActionItem(Icons.Default.Notifications, "Alerts") {
                        context.startActivity(Intent(context, UserAnnouncementListActivity::class.java))
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                when {
                    sLoading -> {
                        CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
                    }
                    assignedSchedule != null && assignedSchedule?.scheduleId?.isNotEmpty() == true -> {
                        RouteDetailCards(schedule = assignedSchedule!!)
                    }
                    else -> {
                        Text("No schedule assigned for today.", color = Color.Gray)
                    }
                }

                Spacer(modifier = Modifier.weight(1f))

                // MAIN ACTION BUTTON with Gradient (Matches Login)
                val isTripActive = activeTrip != null
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp)
                        .height(56.dp)
                        .background(
                            brush = if (isTripActive) Brush.horizontalGradient(colors = listOf(Red, Color(0xFFFF5252)))
                            else Brush.horizontalGradient(colors = ButtonColor),
                            shape = RoundedCornerShape(15.dp)
                        )
                        .clickable {
                            if (!isTripActive) {
                                if (assignedSchedule == null) Toast.makeText(context, "No schedule assigned", Toast.LENGTH_SHORT).show()
                                else tripViewModel.startTripWithValidation(assignedSchedule!!) { s, m -> Toast.makeText(context, m, Toast.LENGTH_SHORT).show() }
                            } else {
                                showEndTripDialog = true
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (isTripActive) "End Collection Route" else "Start Collection Route",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = White
                    )
                }
            }
        }
    }
}

@Composable
fun QuickActionItem(icon: ImageVector, label: String, onClick: () -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Surface(
            modifier = Modifier
                .size(65.dp)
                .clickable { onClick() },
            shape = CircleShape,
            color = Color.White,
            shadowElevation = 2.dp
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = icon,
                    contentDescription = label,
                    tint = Blue, // Using Blue from your theme
                    modifier = Modifier.size(28.dp)
                )
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(text = label, fontSize = 12.sp, color = Color.DarkGray, fontWeight = FontWeight.Medium)
    }
}



@Composable

fun RouteDetailCards(schedule: ScheduleModel) {

    Card(

        modifier = Modifier.fillMaxWidth(),

        shape = RoundedCornerShape(16.dp),

        colors = CardDefaults.cardColors(containerColor = Color(0xFFF1F8E9)) // Light green tint

    ) {

        Column(modifier = Modifier.padding(16.dp)) {

            Row(verticalAlignment = Alignment.CenterVertically) {

                Icon(Icons.Default.Route, contentDescription = null, tint = Green)

                Spacer(modifier = Modifier.width(8.dp))

                Text(

                    text = schedule.routeName,

                    style = TextStyle(fontSize = 20.sp, fontWeight = FontWeight.Bold)

                )

            }



            Spacer(modifier = Modifier.height(8.dp))



            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {

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

