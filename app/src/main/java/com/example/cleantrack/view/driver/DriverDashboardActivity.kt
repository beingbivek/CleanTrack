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
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Route
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.cleantrack.model.AnnouncementModel
import com.example.cleantrack.model.ScheduleModel
import com.example.cleantrack.repository.*
import com.example.cleantrack.ui.theme.*
import com.example.cleantrack.view.common.AnnouncementBanner
import com.example.cleantrack.view.common.LogoutDialog
import com.example.cleantrack.viewmodel.ActiveTripViewModel
import com.example.cleantrack.viewmodel.AnnouncementViewModel
import com.example.cleantrack.viewmodel.ScheduleViewModel
import com.example.cleantrack.viewmodel.UserViewModel
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import java.text.SimpleDateFormat
import java.util.*

private lateinit var fusedLocationClient: FusedLocationProviderClient
class DriverDashboardActivity : ComponentActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        enableEdgeToEdge()
        setContent {
            DriverDashboardScreen()
        }
    }
}

@Composable
fun DriverDashboardScreen() {
    val context = LocalContext.current



    // FIX 1: Pass all required repositories to the ViewModel constructor
    val userViewModel = remember { UserViewModel(UserRepoImpl()) }
    val scheduleViewModel = remember { ScheduleViewModel(ScheduleRepoImpl()) }
    val announcementVM = remember { AnnouncementViewModel(AnnouncementRepoImpl()) }
    var showEndTripDialog by remember { mutableStateOf(false) }
    val tripViewModel = remember {
        ActiveTripViewModel(
            repo = ActiveTripRepoImpl(),
            userRepo = UserRepoImpl(),
            binRepo = BinRepoImpl(),
            collectionRepo = BinCollectionRepoImpl()
        )
    }

    val currentUserId = userViewModel.getCurrentUserId() ?: ""
    val currentUser by userViewModel.user!!.observeAsState()


    // State Observers
    val announcements by announcementVM.allAnnouncements.observeAsState(emptyList())
    val activeTrip by tripViewModel.activeTrip.observeAsState()

    // UI State
    var showAnnouncement by remember { mutableStateOf(false) }
    var latestUnseenAnnouncement by remember { mutableStateOf<AnnouncementModel?>(null) }
    var showLogoutDialog by remember { mutableStateOf(false) }
    val sLoading by scheduleViewModel.loading.observeAsState(false)
    val assignedSchedule by scheduleViewModel.schedule.observeAsState(null)

    val locationCallback = remember {
        object : com.google.android.gms.location.LocationCallback() {
            override fun onLocationResult(result: com.google.android.gms.location.LocationResult) {
                result.lastLocation?.let { location ->
                    // This updates Firebase and your UI instantly
                    activeTrip?.let { trip ->
                        tripViewModel.updateLocation(trip.tripId, location.latitude, location.longitude)
                        Log.d("CLEANTRACK", "STREAMING GPS: ${location.latitude}, ${location.longitude}")
                    }
                }
            }
        }
    }

    // Initial Data Fetch
    LaunchedEffect(currentUserId) {
        if (currentUserId.isNotEmpty()) {
            userViewModel.getUserById(currentUserId)
            announcementVM.getAllAnnouncements { _, _, _ -> }
            scheduleViewModel.getScheduleByDriver(currentUserId)
        }
    }

    // FIX 2: Handle null-safety for delegated property 'announcements'
    LaunchedEffect(announcements) {
        val list = announcements ?: emptyList()
        if (list.isNotEmpty()) {
            val unseen = list.firstOrNull { announcement ->
                !(announcement.seenBy[currentUserId] ?: false)
            }
            if (unseen != null) {
                latestUnseenAnnouncement = unseen
                showAnnouncement = true
            }
        }
    }
    // Automatically resume the active trip if one exists for the assigned route
    LaunchedEffect(assignedSchedule) {
        assignedSchedule?.routeId?.let { routeId ->
            if (routeId.isNotEmpty()) {
                tripViewModel.observeActiveTripByRoute(routeId)
            }
        }
    }

// Automatically start location tracking if an active trip is detected
    // 2. Logic to start the 2-second location loop once activeTrip is NOT null
    LaunchedEffect(activeTrip) {
        val trip = activeTrip
        if (trip != null && trip.status == "ACTIVE") {
            val locationRequest = com.google.android.gms.location.LocationRequest.Builder(
                com.google.android.gms.location.Priority.PRIORITY_HIGH_ACCURACY,
                2000 // Update every 2 seconds
            ).setMinUpdateIntervalMillis(1000) // Minimum 1 second between updates
                .build()

            try {
                fusedLocationClient.requestLocationUpdates(
                    locationRequest,
                    locationCallback,
                    android.os.Looper.getMainLooper()
                )
            } catch (e: SecurityException) {
                Log.e("CLEANTRACK", "Permission error", e)
            }
        } else {
            // Stop tracking if trip is null or COMPLETED
            fusedLocationClient.removeLocationUpdates(locationCallback)
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            fusedLocationClient.removeLocationUpdates(locationCallback)
        }
    }

    LogoutDialog(
        showDialog = showLogoutDialog,
        onDismiss = { showLogoutDialog = false },
        viewModel = userViewModel
    )

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = { showLogoutDialog = true }) {
                Icon(Icons.Default.Logout, contentDescription = "Logout", tint = Red)
            }
        }
    ) { innerPadding ->
        Box(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(White)
                    .padding(20.dp)
            ) {
                if (showAnnouncement && latestUnseenAnnouncement != null) {
                    AnnouncementBanner(
                        announcement = latestUnseenAnnouncement!!,
                        onDismiss = {
                            showAnnouncement = false
                            announcementVM.markAsSeen(latestUnseenAnnouncement!!.id, currentUserId)
                        }
                    )
                }

                Spacer(modifier = Modifier.height(40.dp))

                Text(
                    text = "Driver Dashboard",
                    style = TextStyle(fontSize = 30.sp, fontWeight = FontWeight.ExtraBold, color = Black, textAlign = TextAlign.Center),
                    modifier = Modifier.fillMaxWidth()
                )

                Text(
                    text = "Welcome ${currentUser?.fullname ?: "Driver"} 👋",
                    style = TextStyle(fontSize = 18.sp, color = Black, textAlign = TextAlign.Center),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(25.dp))

                when {
                    sLoading -> {
                        CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
                    }
                    assignedSchedule != null && assignedSchedule?.scheduleId?.isNotEmpty() == true -> {
                        RouteDetailCard(schedule = assignedSchedule!!)
                    }
                    else -> {
                        Text("No schedule assigned for today.", color = Color.Gray)
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                DashboardCard(
                    title = "Today's Overview",
                    content = "View your assigned tasks and progress for the current route.",
                    buttonText = "View Route Map",
                    onButtonClick = {
                        val intent = Intent(context, DriverLocationMapActivity::class.java)
                        context.startActivity(intent)
                    }
                )

                Spacer(modifier = Modifier.height(20.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(TextBoxColor, shape = RoundedCornerShape(18.dp))
                        .padding(20.dp)
                ) {
                    Column {
                        Text("Assigned Route", fontSize = 20.sp, fontWeight = FontWeight.SemiBold, color = Black)
                        Text(activeTrip?.routeName ?: "No Active Route", fontSize = 18.sp, color = Green, fontWeight = FontWeight.Bold)

                        Spacer(modifier = Modifier.height(15.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            val stats by tripViewModel.binStats.observeAsState(Triple(0, 0, 0))

                            RouteStat(
                                label = "Collected",
                                value = stats.second.toString(),
                                color = Green
                            )
                            RouteStat(
                                label = "Remains",
                                value = stats.third.toString(),
                                color = Red
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(30.dp))

                val statusText = if (activeTrip?.status == "ACTIVE") "Status: Route Active" else "Status: Waiting to Start"
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(if (activeTrip != null) Color(0xFFE8F5E9) else TextBoxColor, RoundedCornerShape(16.dp))
                        .padding(20.dp)
                ) {
                    Text(text = statusText, fontSize = 18.sp, fontWeight = FontWeight.Medium, color = Green)
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Determine if a trip is currently running
                val isTripActive = activeTrip != null

// --- END TRIP CONFIRMATION DIALOG ---
                if (showEndTripDialog) {
                    AlertDialog(
                        onDismissRequest = { showEndTripDialog = false },
                        title = { Text("End Route?") },
                        text = { Text("Are you sure you want to end the collection route? This will stop live tracking.") },
                        confirmButton = {
                            Button(
                                onClick = {
                                    showEndTripDialog = false
                                    activeTrip?.let { trip ->
                                        tripViewModel.endTrip(trip.tripId) { success, msg ->
                                            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                            ) {
                                Text("End Route", color = Color.White)
                            }
                        },
                        dismissButton = {
                            TextButton(onClick = { showEndTripDialog = false }) {
                                Text("Cancel")
                            }
                        }
                    )
                }

// --- THE MAIN ACTION BUTTON ---
                Button(
                    onClick = {
                        if (!isTripActive) {
                            val schedule = assignedSchedule
                            if (schedule == null || schedule.scheduleId.isBlank()) {
                                Toast.makeText(context, "No schedule assigned for today.", Toast.LENGTH_SHORT).show()
                            } else {
                                Log.d("CLEANTRACK", "Attempting to start: ${schedule.scheduleId}")
                                tripViewModel.startTripWithValidation(schedule) { success, msg ->
                                    Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                                }
                            }
                        } else {
                            showEndTripDialog = true
                        }
                    },
//                    onClick = {
//                        if (!isTripActive) {
//                            if (assignedSchedule == null || assignedSchedule?.scheduleId?.isEmpty() == true) {
//                                Toast.makeText(context, "No schedule found to start.", Toast.LENGTH_SHORT).show()
//                            } else {
//                                // FIX: Use the correct ViewModel function and explicit types
//                                tripViewModel.startTripWithValidation(assignedSchedule!!) { success: Boolean, msg: String ->
//                                    if (success) {
//                                        val tripId = tripViewModel.activeTrip.value?.tripId ?: ""
//                                        tripViewModel.startLocationTracking(tripId) {
//                                            try {
//                                                fusedLocationClient.lastLocation.addOnSuccessListener { location ->
//                                                    location?.let {
//                                                        tripViewModel.updateLocation(tripId, it.latitude, it.longitude)
//                                                    }
//                                                }
//                                            } catch (e: SecurityException) { e.printStackTrace() }
//                                        }
//                                    }
//                                    Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
//                                }
//                            }
//                        } else {
//                            showEndTripDialog = true
//                        }
//                    },
                    modifier = Modifier.fillMaxWidth().height(65.dp),
                    shape = RoundedCornerShape(18.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isTripActive) Color.Red else Color(0xFF4CAF50)
                    )
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = if (isTripActive) "End Collection Route" else "Start Collection Route",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        if (!isTripActive && assignedSchedule != null) {
                            Text(
                                text = "Route: ${assignedSchedule?.routeName}",
                                fontSize = 12.sp,
                                color = Color.White.copy(alpha = 0.8f)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                if (activeTrip != null) {
                    Button(
                        onClick = {
                            context.startActivity(Intent(context, DriverScanBinActivity::class.java))
                        },
                        modifier = Modifier.fillMaxWidth().height(60.dp).background(ButtonColor.first(), RoundedCornerShape(18.dp)),
                        colors = ButtonDefaults.buttonColors(containerColor = Transparent)
                    ) {
                        Text("Scan Bin QR", fontSize = 20.sp, color = White, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun DashboardCard(title: String, content: String, buttonText: String, onButtonClick: () -> Unit) {
    Box(modifier = Modifier.fillMaxWidth().background(TextBoxColor, shape = RoundedCornerShape(18.dp)).padding(20.dp)) {
        Column {
            Text(text = title, fontSize = 20.sp, fontWeight = FontWeight.SemiBold, color = Black)
            Spacer(modifier = Modifier.height(10.dp))
            Text(text = content, fontSize = 14.sp, color = Black)
            Spacer(modifier = Modifier.height(15.dp))
            Button(
                onClick = onButtonClick,
                modifier = Modifier.fillMaxWidth().height(50.dp).background(Brush.horizontalGradient(colors = ButtonColor), shape = RoundedCornerShape(12.dp)),
                colors = ButtonDefaults.buttonColors(containerColor = Transparent)
            ) {
                Text(buttonText, color = White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun RouteStat(label: String, value: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, fontSize = 22.sp, fontWeight = FontWeight.Bold, color = color)
        Text(label, fontSize = 14.sp, color = Black)
    }
}

@Composable
fun RouteDetailCard(schedule: ScheduleModel) {
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