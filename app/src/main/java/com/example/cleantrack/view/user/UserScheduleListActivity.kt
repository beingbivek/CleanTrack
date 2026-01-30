package com.example.cleantrack.view.user

import android.app.Activity
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.cleantrack.model.ScheduleModel
import com.example.cleantrack.repository.ScheduleRepoImpl
import com.example.cleantrack.repository.UserRepoImpl
import com.example.cleantrack.ui.theme.*
import com.example.cleantrack.viewmodel.ScheduleViewModel
import com.example.cleantrack.viewmodel.UserViewModel

class UserScheduleListActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            UserScheduleListScreen()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserScheduleListScreen() {
    val context = LocalContext.current
    val activity = context as Activity
    val prefManager = remember { com.example.cleantrack.util.PreferenceManager(context) }
    val isOfflineMode = activity.intent.getBooleanExtra("OFFLINE_MODE", false)

    val scheduleVM = remember { ScheduleViewModel(ScheduleRepoImpl()) }
    val userVM = remember { UserViewModel(UserRepoImpl()) }

    // FIX 1: Observe as null so we can distinguish "Loading" from "Empty"
    val allSchedules by scheduleVM.schedules.observeAsState(null)
    val drivers by userVM.drivers.observeAsState(emptyList())
    val userProfile by userVM.user.observeAsState()
    val loading by scheduleVM.loading.observeAsState(false)

    val driverMap = remember(drivers) {
        drivers?.associateBy(keySelector = { it.userId }, valueTransform = { it.fullname }) ?: emptyMap()
    }

    var selectedScheduleForDetail by remember { mutableStateOf<ScheduleModel?>(null) }
    val sheetState = rememberModalBottomSheetState()
    var showSheet by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        if (isOfflineMode) {
            val cachedData = prefManager.getSavedSchedules()
            scheduleVM.schedules.postValue(cachedData)
        } else {
            scheduleVM.getAllSchedules()
            userVM.getAllDrivers()
            userVM.getCurrentUserId()?.let { userVM.getUserById(it) }
        }
    }

    val mySchedules = remember(allSchedules, userProfile, isOfflineMode) {
        if (isOfflineMode) {
            allSchedules ?: emptyList()
        } else {
            allSchedules?.filter { it.routeId == userProfile?.activeRouteId && it.active } ?: emptyList()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(Blue, Green, Color.White),
                    startY = 0f,
                    endY = 1300f
                )
            )
    ) {
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                CenterAlignedTopAppBar(
                    title = { Text("Collection Schedules", fontWeight = FontWeight.ExtraBold, color = Color.White) },
                    navigationIcon = {
                        IconButton(onClick = { activity.finish() }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                        }
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.Transparent)
                )
            }
        ) { padding ->
            Column(modifier = Modifier.fillMaxSize().padding(padding)) {
                if (isOfflineMode) {
                    Card(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 8.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFE91E63)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = "Offline Mode: Viewing cached schedules.",
                            modifier = Modifier.padding(12.dp).fillMaxWidth(),
                            textAlign = TextAlign.Center,
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Box(modifier = Modifier.fillMaxSize().weight(1f)) {
                    // FIX 2: Correct loading logic
                    if (loading || (allSchedules == null && !isOfflineMode)) {
                        CircularProgressIndicator(
                            modifier = Modifier.align(Alignment.Center),
                            color = Green // Changed from White to Green for visibility
                        )
                    } else if (!isOfflineMode && userProfile?.activeRouteId.isNullOrEmpty()) {
                        EmptyStateView(
                            title = "No Route Selected",
                            description = "Please go to 'Routes' and confirm your neighborhood route."
                        )
                    } else if (mySchedules.isEmpty()) {
                        EmptyStateView(
                            title = "No Upcoming Pickups",
                            description = "There are currently no active schedules for your route."
                        )
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            items(mySchedules) { schedule ->
                                UserScheduleCard(
                                    schedule = schedule,
                                    driverName = driverMap[schedule.driverId] ?: "Assigned Driver",
                                    onClick = {
                                        selectedScheduleForDetail = schedule
                                        showSheet = true
                                    }
                                )
                            }
                        }
                    }
                }
            }

            if (showSheet && selectedScheduleForDetail != null) {
                ModalBottomSheet(
                    onDismissRequest = { showSheet = false },
                    sheetState = sheetState,
                    containerColor = Color.White,
                    dragHandle = { BottomSheetDefaults.DragHandle() }
                ) {
                    ScheduleDetailContent(
                        schedule = selectedScheduleForDetail!!,
                        driverName = driverMap[selectedScheduleForDetail!!.driverId] ?: "Unknown Driver"
                    )
                }
            }
        }
    }
}

// ... Keep existing Composable functions: UserScheduleCard, ScheduleDetailContent, DetailRow, EmptyStateView ...
@Composable
fun UserScheduleCard(schedule: ScheduleModel, driverName: String, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable { onClick() },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = White),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Row(modifier = Modifier.padding(20.dp), verticalAlignment = Alignment.CenterVertically) {
            Surface(shape = CircleShape, color = Green.copy(alpha = 0.1f), modifier = Modifier.size(45.dp)) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(Icons.Default.Schedule, null, tint = Green, modifier = Modifier.size(24.dp))
                }
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = schedule.routeName, fontWeight = FontWeight.ExtraBold, fontSize = 16.sp, color = Black)
                Text(text = "${schedule.dayOfWeek} | ${schedule.startTime}", color = Color.DarkGray, fontSize = 14.sp)
                Text(text = "Driver: $driverName", fontSize = 12.sp, color = Green, fontWeight = FontWeight.Bold)
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(text = "Truck", fontSize = 10.sp, color = Color.Gray)
                Text(text = schedule.vehicleNumber, fontWeight = FontWeight.ExtraBold, fontSize = 14.sp, color = Black)
            }
        }
    }
}

@Composable
fun ScheduleDetailContent(schedule: ScheduleModel, driverName: String) {
    Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 16.dp).padding(bottom = 40.dp)) {
        Text(text = "Pickup Details", fontSize = 22.sp, fontWeight = FontWeight.ExtraBold, color = Black)
        Spacer(modifier = Modifier.height(20.dp))
        DetailRow(label = "Neighborhood", value = schedule.routeName)
        DetailRow(label = "Collection Day", value = schedule.dayOfWeek)
        DetailRow(label = "Window", value = "${schedule.startTime} - ${schedule.endTime}")
        DetailRow(label = "Vehicle Plate", value = schedule.vehicleNumber)
        DetailRow(label = "Assigned Driver", value = driverName)
        Spacer(modifier = Modifier.height(24.dp))
        Surface(color = if (schedule.active) Green.copy(0.1f) else Color.Red.copy(0.1f), shape = RoundedCornerShape(12.dp)) {
            Row(modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.size(8.dp).background(if(schedule.active) Green else Color.Red, CircleShape))
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = if (schedule.active) "ACTIVE SCHEDULE" else "INACTIVE", color = if (schedule.active) Green else Color.Red, fontWeight = FontWeight.ExtraBold)
            }
        }
    }
}

@Composable
fun DetailRow(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 10.dp), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(text = label, color = Color.DarkGray, fontWeight = FontWeight.Medium)
        Text(text = value, color = Black, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun EmptyStateView(title: String, description: String) {
    Column(modifier = Modifier.fillMaxSize().padding(32.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
        Text(title, fontWeight = FontWeight.ExtraBold, fontSize = 20.sp, color = Color.Gray)
        Spacer(modifier = Modifier.height(8.dp))
        Text(description, textAlign = TextAlign.Center, color = Color.Gray.copy(0.8f), fontSize = 14.sp)
    }
}