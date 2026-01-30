package com.example.cleantrack.view.driver

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.cleantrack.model.ScheduleModel
import com.example.cleantrack.repository.*
import com.example.cleantrack.ui.theme.BackgroundLightGray
import com.example.cleantrack.ui.theme.Black
import com.example.cleantrack.ui.theme.Blue
import com.example.cleantrack.ui.theme.ButtonColor
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

fun getDayOrder(day: String): Int {
    return when (day.lowercase()) {
        "sunday" -> 0
        "monday" -> 1
        "tuesday" -> 2
        "wednesday" -> 3
        "thursday" -> 4
        "friday" -> 5
        "saturday" -> 6
        else -> 7
    }
}

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
    } catch (e: Exception) { false }
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

    LaunchedEffect(driverId) {
        if (driverId.isNotEmpty()) viewModel.loadDriverSchedules(driverId)
    }

    val currentActiveSchedule = remember(schedules) {
        schedules?.find { isScheduleCurrentlyActive(it) }
    }

    LaunchedEffect(currentActiveSchedule) {
        currentActiveSchedule?.routeId?.let { tripViewModel.observeActiveTripByRoute(it) }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(colors = listOf(Blue, Green, Color.White), startY = 0f, endY = 1800f))
    ) {
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                CenterAlignedTopAppBar(
                    title = { Text("Weekly Routine", fontWeight = FontWeight.ExtraBold, color = Color.White) },
                    navigationIcon = {
                        IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, null, tint = Color.White) }
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.Transparent)
                )
            }
        ) { padding ->
            Box(modifier = Modifier.fillMaxSize().padding(padding)) {
                if (isLoading == true) {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center), color = Color.White)
                } else if (schedules.isNullOrEmpty()) {
                    Text("No routine found.", color = Color.White, modifier = Modifier.align(Alignment.Center))
                } else {
                    LazyColumn(modifier = Modifier.fillMaxSize(), contentPadding = PaddingValues(bottom = 20.dp)) {

                        // 1. ACTIVE SHIFT
                        if (currentActiveSchedule != null) {
                            item { ActiveSectionHeader("Current Active Shift") }
                            item {
                                val isTracking = activeTrip?.status == "ACTIVE" && activeTrip?.routeId == currentActiveSchedule.routeId
                                ActiveScheduleCard(currentActiveSchedule, isTracking) { selectedSchedule = currentActiveSchedule }
                            }
                        }

                        // 2. SORTED WEEKLY LIST
                        val sortedGrouped = schedules
                            ?.filter { it.scheduleId != currentActiveSchedule?.scheduleId }
                            ?.groupBy { it.dayOfWeek }
                            ?.toSortedMap(compareBy { getDayOrder(it) })

                        sortedGrouped?.forEach { (day, dailySchedules) ->
                            item { RoutineDayHeader(day) }
                            items(dailySchedules.sortedBy { it.startTime }) { schedule ->
                                ScheduleCard(schedule) { selectedSchedule = schedule }
                            }
                        }
                    }
                }
            }
            selectedSchedule?.let { ScheduleDetailPopup(it) { selectedSchedule = null } }
        }
    }
}

@Composable
fun ActiveSectionHeader(title: String) {
    Row(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
        Icon(Icons.Default.FlashOn, null, tint = Color.White, modifier = Modifier.size(20.dp))
        Spacer(modifier = Modifier.width(8.dp))
        Text(text = title, fontSize = 16.sp, fontWeight = FontWeight.ExtraBold, color = Color.White)
    }
}

@Composable
fun ActiveScheduleCard(schedule: ScheduleModel, isTracking: Boolean, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp).clickable { onClick() },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(0.2f)),
        border = BorderStroke(1.dp, Color.White.copy(0.5f))
    ) {
        Row(modifier = Modifier.padding(20.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(50.dp).background(if(isTracking) Color.Green else Color.White, CircleShape), contentAlignment = Alignment.Center) {
                Icon(if(isTracking) Icons.Default.GpsFixed else Icons.Default.PlayArrow, null, tint = if(isTracking) Color.White else Green)
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(schedule.routeName, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White)
                // Day shown inside Active Card too
                Text(schedule.dayOfWeek, color = Color.White.copy(0.7f), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                Text(if(isTracking) "On Route Now" else "Pending Shift", color = if(isTracking) Color.Green else Color.White.copy(0.8f), fontSize = 14.sp)
            }
            Spacer(modifier = Modifier.weight(1f))
            Icon(Icons.Default.ChevronRight, null, tint = Color.White)
        }
    }
}

@Composable
fun RoutineDayHeader(day: String) {
    // Background changed to a light green tint and text to dark Green for visibility on white background
    Surface(modifier = Modifier.fillMaxWidth().padding(top = 10.dp), color = Green.withAlpha(0.1f)) {
        Text(
            text = day.uppercase(),
            fontSize = 13.sp,
            fontWeight = FontWeight.Black,
            color = Black, // Changed to Green for visibility
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
        )
    }
}

@Composable
fun ScheduleCard(schedule: ScheduleModel, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 6.dp).clickable { onClick() },
        shape = RoundedCornerShape(15.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(40.dp).background(Green.copy(0.1f), CircleShape), contentAlignment = Alignment.Center) {
                Icon(Icons.Default.Schedule, null, tint = Green, modifier = Modifier.size(20.dp))
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(schedule.routeName, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.DarkGray)
                // Day added here inside the white card
                Text(schedule.dayOfWeek, fontSize = 12.sp, color = Green, fontWeight = FontWeight.SemiBold)
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
        Card(shape = RoundedCornerShape(28.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
            Column(modifier = Modifier.padding(24.dp)) {
                Text("Shift Details", fontSize = 22.sp, fontWeight = FontWeight.ExtraBold, color = Green)
                Spacer(modifier = Modifier.height(20.dp))
                DetailItem(Icons.Default.Map, "Route", schedule.routeName)
                DetailItem(Icons.Default.Event, "Day", schedule.dayOfWeek)
                DetailItem(Icons.Default.AccessTime, "Time", "${schedule.startTime} - ${schedule.endTime}")
                DetailItem(Icons.Default.LocalShipping, "Truck No", schedule.vehicleNumber)
                Spacer(modifier = Modifier.height(25.dp))
                Button(onClick = onDismiss, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = Green), shape = RoundedCornerShape(15.dp)) {
                    Text("Got it", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun DetailItem(icon: ImageVector, label: String, value: String) {
    Row(modifier = Modifier.padding(vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, null, tint = Green, modifier = Modifier.size(22.dp))
        Spacer(modifier = Modifier.width(15.dp))
        Column {
            Text(label, fontSize = 12.sp, color = Color.Gray)
            Text(value, fontSize = 16.sp, fontWeight = FontWeight.Bold)
        }
    }
}

// Extension to help with alpha in older Compose versions if needed
fun Color.withAlpha(alpha: Float): Color = this.copy(alpha = alpha)