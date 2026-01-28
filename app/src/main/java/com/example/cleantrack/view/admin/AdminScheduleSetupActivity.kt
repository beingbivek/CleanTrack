package com.example.cleantrack.view.admin

import android.app.Activity
import android.app.TimePickerDialog
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import com.example.cleantrack.model.ScheduleModel
import com.example.cleantrack.model.NotificationPayload
import com.example.cleantrack.repository.*
import com.example.cleantrack.ui.theme.PrimaryGreen
import com.example.cleantrack.viewmodel.*
import java.util.Calendar

class AdminScheduleSetupActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val scheduleId = intent.getStringExtra("SCHEDULE_ID")
        setContent {
            AdminScheduleSetupScreen(scheduleId)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminScheduleSetupScreen(scheduleId: String?) {
    val context = LocalContext.current
    val activity = context as Activity

    val scheduleVM = remember { ScheduleViewModel(ScheduleRepoImpl()) }
    val routeVM = remember { RouteViewModel(RouteRepoImpl()) }
    val vehicleVM = remember { VehicleViewModel(VehicleRepoImpl()) }
    val userVM = remember { UserViewModel(UserRepoImpl()) }
    val notificationVM = remember { NotificationViewModel(NotificationRepoImpl(), UserRepoImpl()) }

    val selectedSchedule by scheduleVM.schedule.observeAsState(null)
    val schedules by scheduleVM.schedules.observeAsState(null)
    val loading by scheduleVM.loading.observeAsState(false)

    val routes by routeVM.routes.observeAsState(emptyList())
    val vehicles by vehicleVM.vehicles.observeAsState(emptyList())
    val drivers by userVM.drivers.observeAsState(emptyList())

    // UI STATE
    var routeId by remember { mutableStateOf("") }
    var routeName by remember { mutableStateOf("") }
    var vehicleId by remember { mutableStateOf("") }
    var vehicleNumber by remember { mutableStateOf("") }
    var driverId by remember { mutableStateOf("") }
    var driverName by remember { mutableStateOf("") }
    var dayOfWeek by remember { mutableStateOf("") }
    var startTime by remember { mutableStateOf("") }
    var endTime by remember { mutableStateOf("") }

    var isSaving by remember { mutableStateOf(false) }

    val daysOfWeek = listOf("Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday")

    LaunchedEffect(Unit) {
        routeVM.loadRoutes()
        vehicleVM.getAllVehicles()
        userVM.getAllDrivers()
        scheduleVM.getAllSchedules()
    }

    LaunchedEffect(scheduleId) {
        scheduleId?.let { scheduleVM.getScheduleById(it) }
    }

    LaunchedEffect(selectedSchedule) {
        selectedSchedule?.let {
            routeId = it.routeId
            routeName = it.routeName
            vehicleId = it.vehicleId
            vehicleNumber = it.vehicleNumber
            driverId = it.driverId
            dayOfWeek = it.dayOfWeek
            startTime = it.startTime
            endTime = it.endTime
        }
    }

    LaunchedEffect(drivers, driverId) {
        if (driverId.isNotBlank()) {
            driverName = drivers?.firstOrNull { it.userId == driverId }?.fullname ?: ""
        }
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
                        Text(
                            if (scheduleId == null) "Add Schedule" else "Edit Schedule",
                            style = TextStyle(color = Color.White, fontWeight = FontWeight.ExtraBold, fontSize = 20.sp)
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = { activity.finish() }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = Color.White)
                        }
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.Transparent)
                )
            }
        ) { padding ->
            if (loading) {
                Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = PrimaryGreen)
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .padding(horizontal = 20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    contentPadding = PaddingValues(top = 10.dp, bottom = 40.dp)
                ) {
                    item {
                        DropdownField(
                            label = "Select Route",
                            value = routeName,
                            options = routes.map { it.name },
                            onSelect = { name ->
                                val route = routes.first { it.name == name }
                                routeId = route.routeId
                                routeName = route.name
                            }
                        )
                    }

                    item {
                        val activeVehicles = vehicles.filter { it.active }
                        DropdownField(
                            label = "Select Vehicle",
                            value = vehicleNumber,
                            options = activeVehicles.map { it.vehicleNumber },
                            onSelect = { num ->
                                val vehicle = activeVehicles.first { it.vehicleNumber == num }
                                vehicleId = vehicle.vehicleId
                                vehicleNumber = vehicle.vehicleNumber
                            }
                        )
                    }

                    item {
                        DropdownField(
                            label = "Select Driver",
                            value = driverName,
                            options = drivers?.map { it.fullname },
                            onSelect = { name ->
                                val driver = drivers?.first { it.fullname == name }
                                driverId = driver!!.userId
                                driverName = name
                            }
                        )
                    }

                    item {
                        DropdownField(
                            label = "Day of Week",
                            value = dayOfWeek,
                            options = daysOfWeek,
                            onSelect = { dayOfWeek = it }
                        )
                    }

                    item {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            OutlinedTextField(
                                value = startTime,
                                onValueChange = {},
                                modifier = Modifier.weight(1f).clickable { showTimePicker(context) { startTime = it } },
                                label = { Text("Start Time") },
                                enabled = false,
                                readOnly = true,
                                colors = OutlinedTextFieldDefaults.colors(disabledTextColor = Color.Black, disabledBorderColor = Color.Gray, disabledLabelColor = PrimaryGreen)
                            )
                            OutlinedTextField(
                                value = endTime,
                                onValueChange = {},
                                modifier = Modifier.weight(1f).clickable { showTimePicker(context) { endTime = it } },
                                label = { Text("End Time") },
                                enabled = false,
                                readOnly = true,
                                colors = OutlinedTextFieldDefaults.colors(disabledTextColor = Color.Black, disabledBorderColor = Color.Gray, disabledLabelColor = PrimaryGreen)
                            )
                        }
                    }

                    item {
                        Spacer(Modifier.height(10.dp))
                        Button(
                            modifier = Modifier.fillMaxWidth().height(56.dp),
                            onClick = {
                                if (routeId.isBlank() || vehicleId.isBlank() || driverId.isBlank() || dayOfWeek.isBlank() || startTime.isBlank() || endTime.isBlank()) {
                                    Toast.makeText(context, "Please fill all fields", Toast.LENGTH_SHORT).show()
                                    return@Button
                                }
                                if (!isEndTimeAfterStart(startTime, endTime)) {
                                    Toast.makeText(context, "End time must be after start time", Toast.LENGTH_SHORT).show()
                                    return@Button
                                }

                                val model = ScheduleModel(
                                    scheduleId = scheduleId ?: "", routeId = routeId, routeName = routeName,
                                    driverId = driverId, vehicleId = vehicleId, vehicleNumber = vehicleNumber,
                                    dayOfWeek = dayOfWeek, startTime = startTime, endTime = endTime
                                )

                                val conflict = scheduleVM.hasScheduleConflict(model, schedules)
                                if (conflict.first) {
                                    Toast.makeText(context, conflict.second, Toast.LENGTH_LONG).show()
                                    return@Button
                                }

                                isSaving = true
                                val callback: (Boolean, String) -> Unit = { success, msg ->
                                    isSaving = false
                                    Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                                    if (success) {
                                        val payload = NotificationPayload(
                                            title = if (scheduleId == null) "New Schedule Added" else "Schedule Updated",
                                            message = "$routeName schedule set for $dayOfWeek at $startTime.",
                                            type = "schedule", actionType = "schedule",
                                            routeId = routeId, scheduleId = model.scheduleId
                                        )
                                        notificationVM.notifyUsersByRoute(routeId, payload)
                                        notificationVM.notifyDriver(driverId, payload)
                                        activity.finish()
                                    }
                                }

                                if (scheduleId == null) scheduleVM.addSchedule(model, callback)
                                else scheduleVM.updateSchedule(model, callback)
                            },
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen),
                            enabled = !isSaving
                        ) {
                            if (isSaving) {
                                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
                            } else {
                                Text(if (scheduleId == null) "Save Schedule" else "Update Schedule", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DropdownField(label: String, value: String, options: List<String>?, onSelect: (String) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = !expanded }) {
        OutlinedTextField(
            value = value, onValueChange = {}, readOnly = true, label = { Text(label) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
            modifier = Modifier.fillMaxWidth().menuAnchor(),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = PrimaryGreen, focusedLabelColor = PrimaryGreen)
        )
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            options?.forEach {
                DropdownMenuItem(text = { Text(it) }, onClick = { onSelect(it); expanded = false })
            }
        }
    }
}

fun showTimePicker(context: android.content.Context, onTimeSelected: (String) -> Unit) {
    val calendar = Calendar.getInstance()
    TimePickerDialog(context, { _, h, m -> onTimeSelected(String.format("%02d:%02d", h, m)) },
        calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), true).show()
}

fun isEndTimeAfterStart(startTime: String, endTime: String): Boolean {
    return try {
        val start = startTime.split(":").map { it.toInt() }
        val end = endTime.split(":").map { it.toInt() }
        (end[0] * 60 + end[1]) > (start[0] * 60 + start[1])
    } catch (e: Exception) { false }
}