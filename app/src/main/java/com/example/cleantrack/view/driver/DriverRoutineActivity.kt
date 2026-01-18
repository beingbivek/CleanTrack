
package com.example.cleantrack.view.driver

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.cleantrack.model.ScheduleModel
import com.example.cleantrack.repository.*
import com.example.cleantrack.ui.theme.Blue
import com.example.cleantrack.ui.theme.Green
import com.example.cleantrack.viewmodel.ActiveTripViewModel
import com.example.cleantrack.viewmodel.ScheduleViewModel
import java.text.SimpleDateFormat
import java.util.*

class DriverRoutineActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val driverId = intent.getStringExtra("DRIVER_ID") ?: ""
        setContent {
            val scheduleViewModel = remember { ScheduleViewModel(ScheduleRepoImpl()) }
            val tripViewModel = remember {
                ActiveTripViewModel(ActiveTripRepoImpl(), UserRepoImpl(), BinRepoImpl(), BinCollectionRepoImpl(), PointsRepoImpl())
            }

            DriverRoutineScreen(
                driverId = driverId,
                viewModel = scheduleViewModel,
                tripViewModel = tripViewModel,
                onBack = { finish() }
            )
        }
    }
}

/**
 * Helper function to check if a schedule is currently active based on system time
 */
fun isScheduleCurrentlyActive(schedule: ScheduleModel): Boolean {
    return try {
        val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
        val dayFormat = SimpleDateFormat("EEEE", Locale.getDefault())

        val currentTimeStr = sdf.format(Date())
        val currentDay = dayFormat.format(Date())

        if (!schedule.dayOfWeek.equals(currentDay, ignoreCase = true)) return false

        val now = sdf.parse(currentTimeStr)
        val start = sdf.parse(schedule.startTime)
        val end = sdf.parse(schedule.endTime)

        now != null && start != null && end != null && now.after(start) && now.before(end)
    } catch (e: Exception) {
        false
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DriverRoutineScreen(
    driverId: String,
    viewModel: ScheduleViewModel,
    tripViewModel: ActiveTripViewModel,
    onBack: () -> Unit
) {
    val schedules by viewModel.schedules.observeAsState(emptyList())
    val isLoading by viewModel.loading.observeAsState(false)
    val activeTrip by tripViewModel.activeTrip.observeAsState()

    var selectedSchedule by remember { mutableStateOf<ScheduleModel?>(null) }

    // 1. Initial Load of schedules
    LaunchedEffect(driverId) {
        if (driverId.isNotEmpty()) {
            viewModel.loadDriverSchedules(driverId)
        }
    }

    // 2. Identify active schedule based on clock
    val currentActiveSchedule = remember(schedules) {
        schedules?.find { isScheduleCurrentlyActive(it) }
    }

    // 3. KEY FIX: Start observing the specific route status once the active shift is identified
    LaunchedEffect(currentActiveSchedule) {
        currentActiveSchedule?.routeId?.let { routeId ->
            tripViewModel.observeActiveTripByRoute(routeId)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Driver Routine", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, null) }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White, titleContentColor = Blue)
            )
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            if (isLoading == true) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center), color = Blue)
            } else if (schedules.isNullOrEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No routine found.", color = Color.Gray)
                }
            } else {
                LazyColumn(modifier = Modifier.fillMaxSize()) {

                    // --- CURRENT ACTIVE SHIFT SECTION ---
                    if (currentActiveSchedule != null) {
                        item {
                            ActiveSectionHeader("Current Active Shift")
                        }
                        item {
                            // deriving isTracking directly from the live trip state
                            val isCurrentlyTracking = activeTrip?.status == "ACTIVE" &&
                                    activeTrip?.routeId == currentActiveSchedule.routeId

                            ActiveScheduleCard(
                                schedule = currentActiveSchedule,
                                isTracking = isCurrentlyTracking,
                                onClick = { selectedSchedule = currentActiveSchedule }
                            )
                        }
                    }

                    // --- WEEKLY ROUTINE SECTION ---
                    val grouped = schedules?.filter { it.scheduleId != currentActiveSchedule?.scheduleId }
                        ?.sortedBy { it.startTime }
                        ?.groupBy { it.dayOfWeek }

                    grouped?.forEach { entry ->
                        item {
                            RoutineDayHeader(entry.key)
                        }
                        items(entry.value) { schedule ->
                            ScheduleCard(
                                schedule = schedule,
                                onClick = { selectedSchedule = schedule }
                            )
                        }
                    }
                }
            }

            selectedSchedule?.let { schedule ->
                ScheduleDetailPopup(schedule = schedule, onDismiss = { selectedSchedule = null })
            }
        }
    }
}

@Composable
fun ActiveSectionHeader(title: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(Icons.Default.Schedule, contentDescription = null, tint = Green, modifier = Modifier.size(18.dp))
        Spacer(modifier = Modifier.width(8.dp))
        Text(text = title, fontSize = 14.sp, fontWeight = FontWeight.ExtraBold, color = Green)
    }
}

@Composable
fun ActiveScheduleCard(schedule: ScheduleModel, isTracking: Boolean, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Blue),
        elevation = CardDefaults.cardElevation(8.dp)
    ) {
        Row(modifier = Modifier.padding(20.dp), verticalAlignment = Alignment.CenterVertically) {
            Surface(shape = RoundedCornerShape(12.dp), color = Color.White.copy(0.2f), modifier = Modifier.size(50.dp)) {
                Icon(
                    imageVector = if (isTracking) Icons.Default.GpsFixed else Icons.Default.PlayCircleOutline,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.padding(12.dp)
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(schedule.routeName, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White)

                // Status Text: Changes based on live Firebase status
                Text(
                    text = if (isTracking) "Live Tracking Active" else "Ready to Start",
                    fontSize = 13.sp,
                    fontWeight = if (isTracking) FontWeight.Bold else FontWeight.Normal,
                    color = if (isTracking) Color.Green else Color.White.copy(0.9f)
                )
                Text("${schedule.startTime} - ${schedule.endTime}", fontSize = 13.sp, color = Color.White.copy(0.7f))
            }
            Spacer(modifier = Modifier.weight(1f))
            Icon(Icons.Default.ChevronRight, null, tint = Color.White)
        }
    }
}

@Composable
fun RoutineDayHeader(day: String) {
    Surface(modifier = Modifier.fillMaxWidth(), color = Color(0xFFF0F4F8)) {
        Text(
            text = day.uppercase(),
            fontSize = 13.sp,
            fontWeight = FontWeight.Black,
            color = Blue,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )
    }
}

@Composable
fun ScheduleCard(schedule: ScheduleModel, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 6.dp).clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Surface(shape = RoundedCornerShape(8.dp), color = Blue.copy(0.1f), modifier = Modifier.size(44.dp)) {
                Icon(Icons.Default.DirectionsBus, null, tint = Blue, modifier = Modifier.padding(10.dp))
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(schedule.routeName, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                Text("${schedule.startTime} - ${schedule.endTime}", fontSize = 14.sp, color = Color.Gray)
            }
            Spacer(modifier = Modifier.weight(1f))
            Icon(Icons.Default.ChevronRight, null, tint = Color.LightGray)
        }
    }
}

@Composable
fun ScheduleDetailPopup(schedule: ScheduleModel, onDismiss: () -> Unit) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(modifier = Modifier.padding(24.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Route Details", fontSize = 20.sp, fontWeight = FontWeight.ExtraBold, color = Blue)
                    Spacer(modifier = Modifier.weight(1f))
                    IconButton(onClick = onDismiss) { Icon(Icons.Default.Close, null) }
                }
                Spacer(modifier = Modifier.height(16.dp))
                DetailRow(Icons.Default.Map, "Route Name", schedule.routeName)
                DetailRow(Icons.Default.Today, "Day", schedule.dayOfWeek)
                DetailRow(Icons.Default.AccessTime, "Timing", "${schedule.startTime} - ${schedule.endTime}")
                DetailRow(Icons.Default.LocalShipping, "Vehicle No", schedule.vehicleNumber)
                Spacer(modifier = Modifier.height(24.dp))
                Button(onClick = onDismiss, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), colors = ButtonDefaults.buttonColors(containerColor = Blue)) {
                    Text("Close", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun DetailRow(icon: ImageVector, label: String, value: String) {
    Row(modifier = Modifier.padding(vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, null, tint = Blue, modifier = Modifier.size(20.dp))
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(label, fontSize = 12.sp, color = Color.Gray)
            Text(value, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
        }
    }
}