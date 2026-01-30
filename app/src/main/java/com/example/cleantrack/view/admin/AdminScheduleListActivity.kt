package com.example.cleantrack.view.admin

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Schedule
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.cleantrack.model.NotificationPayload
import com.example.cleantrack.model.ScheduleModel
import com.example.cleantrack.repository.NotificationRepoImpl
import com.example.cleantrack.repository.ScheduleRepoImpl
import com.example.cleantrack.repository.UserRepoImpl
import com.example.cleantrack.ui.theme.PrimaryGreen
import com.example.cleantrack.viewmodel.NotificationViewModel
import com.example.cleantrack.viewmodel.ScheduleViewModel
import com.example.cleantrack.viewmodel.UserViewModel
import java.util.*

class AdminScheduleListActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AdminScheduleListScreen()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminScheduleListScreen() {
    val context = LocalContext.current
    val activity = context as? Activity

    val vm = remember { ScheduleViewModel(ScheduleRepoImpl()) }
    val notificationVM = remember { NotificationViewModel(NotificationRepoImpl(), UserRepoImpl()) }
    val dVM = remember { UserViewModel(UserRepoImpl()) }

    val drivers by dVM.drivers.observeAsState(emptyList())
    val schedules by vm.schedules.observeAsState(emptyList())
    val loading by vm.loading.observeAsState(false)

    var showDeleteDialog by remember { mutableStateOf(false) }
    var selectedScheduleId by remember { mutableStateOf("") }
    var selectedSchedule by remember { mutableStateOf<ScheduleModel?>(null) }

    val driverMap = remember(drivers) {
        drivers?.associateBy(keySelector = { it.userId }, valueTransform = { it.fullname }) ?: emptyMap()
    }

    // Define the custom order for sorting days
    val dayOrder = remember {
        mapOf(
            "Sunday" to 0,
            "Monday" to 1,
            "Tuesday" to 2,
            "Wednesday" to 3,
            "Thursday" to 4,
            "Friday" to 5,
            "Saturday" to 6
        )
    }

    // Group and Sort schedules by the custom dayOrder
    val groupedSchedules = remember(schedules) {
        schedules?.groupBy { it.dayOfWeek }
            ?.toSortedMap(compareBy { dayOrder[it] ?: 99 }) ?: emptyMap<String, List<ScheduleModel>>()
    }

    LaunchedEffect(Unit) {
        vm.getAllSchedules()
        dVM.getAllDrivers()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(PrimaryGreen, Color.White),
                    startY = 0f,
                    endY = 1000f
                )
            )
    ) {
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                CenterAlignedTopAppBar(
                    title = {
                        Text("Manage Schedules", style = TextStyle(color = Color.White, fontWeight = FontWeight.ExtraBold, fontSize = 20.sp))
                    },
                    navigationIcon = {
                        IconButton(onClick = { activity?.finish() }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                        }
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.Transparent)
                )
            },
            floatingActionButton = {
                FloatingActionButton(
                    onClick = { context.startActivity(Intent(context, AdminScheduleSetupActivity::class.java)) },
                    containerColor = PrimaryGreen,
                    contentColor = Color.White,
                    shape = CircleShape
                ) {
                    Icon(Icons.Default.Add, null)
                }
            }
        ) { padding ->
            Box(modifier = Modifier.fillMaxSize().padding(padding)) {

                if (!loading && (schedules == null || schedules!!.isEmpty())) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("No schedules found", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Medium)
                            Spacer(Modifier.height(12.dp))
                            Button(
                                onClick = { context.startActivity(Intent(context, AdminScheduleSetupActivity::class.java)) },
                                colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = PrimaryGreen)
                            ) {
                                Text("Add Schedule")
                            }
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(start = 16.dp, top = 8.dp, end = 16.dp, bottom = 100.dp)
                    ) {
                        groupedSchedules.forEach { (day, daySchedules) ->
                            // 🔹 Day Header (Sorted correctly)
                            item {
                                Text(
                                    text = day,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color.Black, // Ensure contrast against the white background
                                    modifier = Modifier.padding(vertical = 12.dp, horizontal = 4.dp)
                                )
                            }

                            // 🔹 Sort by Time inside the day
                            val sortedDaySchedules = daySchedules.sortedBy { it.startTime }

                            items(sortedDaySchedules) { schedule ->
                                ScheduleAdminCard(
                                    schedule = schedule,
                                    driverName = driverMap[schedule.driverId] ?: "Unknown Driver",
                                    onEdit = {
                                        val intent = Intent(context, AdminScheduleSetupActivity::class.java)
                                        intent.putExtra("SCHEDULE_ID", schedule.scheduleId)
                                        context.startActivity(intent)
                                    },
                                    onDelete = {
                                        selectedScheduleId = schedule.scheduleId
                                        selectedSchedule = schedule
                                        showDeleteDialog = true
                                    }
                                )
                            }
                        }
                    }
                }

                // CENTERED LOADING SPINNER
                if (loading) {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center),
                        color = PrimaryGreen,
                        strokeWidth = 4.dp
                    )
                }
            }
        }
    }

    // DELETE CONFIRMATION
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            shape = RoundedCornerShape(20.dp),
            title = { Text("Delete Schedule", fontWeight = FontWeight.Bold) },
            text = { Text("Are you sure you want to delete this schedule? Users and drivers will be notified.") },
            confirmButton = {
                TextButton(onClick = {
                    vm.deleteSchedule(selectedScheduleId) { success, msg ->
                        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                        if (success) {
                            selectedSchedule?.let { schedule ->
                                val payload = NotificationPayload(
                                    title = "Schedule Removed",
                                    message = "${schedule.routeName} schedule has been cancelled.",
                                    type = "schedule",
                                    actionType = "schedule",
                                    routeId = schedule.routeId,
                                    scheduleId = schedule.scheduleId
                                )
                                notificationVM.notifyUsersByRoute(schedule.routeId, payload)
                                notificationVM.notifyDriver(schedule.driverId, payload)
                            }
                            vm.getAllSchedules()
                            showDeleteDialog = false
                        }
                    }
                }) {
                    Text("Delete", color = Color.Red, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancel", color = Color.Gray)
                }
            }
        )
    }
}

@Composable
fun ScheduleAdminCard(
    schedule: ScheduleModel,
    driverName: String,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(50.dp)
                    .background(PrimaryGreen.copy(alpha = 0.1f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Schedule, null, tint = PrimaryGreen, modifier = Modifier.size(26.dp))
            }

            Spacer(Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(schedule.routeName, fontWeight = FontWeight.Bold, fontSize = 17.sp, color = Color.Black)
                Text("Driver: $driverName", fontSize = 14.sp, color = Color.DarkGray)
                Text("${schedule.startTime} - ${schedule.endTime}", fontSize = 13.sp, color = PrimaryGreen, fontWeight = FontWeight.Medium)
                Text("Vehicle: ${schedule.vehicleNumber}", fontSize = 13.sp, color = Color.Gray)
            }

            Row {
                IconButton(onClick = onEdit) {
                    Icon(Icons.Default.Edit, contentDescription = "Edit", tint = PrimaryGreen)
                }
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color.Red)
                }
            }
        }
    }
}