package com.example.cleantrack.view.admin

import android.app.Activity
import android.app.TimePickerDialog
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.cleantrack.model.ScheduleModel
import com.example.cleantrack.repository.RouteRepoImpl
import com.example.cleantrack.repository.ScheduleRepoImpl
import com.example.cleantrack.repository.UserRepoImpl
import com.example.cleantrack.repository.VehicleRepoImpl
import com.example.cleantrack.view.auth.DropdownField
import com.example.cleantrack.viewmodel.RouteViewModel
import com.example.cleantrack.viewmodel.ScheduleViewModel
import com.example.cleantrack.viewmodel.UserViewModel
import com.example.cleantrack.viewmodel.VehicleViewModel
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

    val daysOfWeek = listOf(
        "Sunday", "Monday", "Tuesday",
        "Wednesday", "Thursday", "Friday", "Saturday"
    )


    // LOAD DATA
    LaunchedEffect(Unit) {
        routeVM.loadRoutes()
        vehicleVM.getAllVehicles()
        userVM.getAllDrivers()   // role == DRIVER
    }

    // LOAD SCHEDULE FOR EDIT
    LaunchedEffect(scheduleId) {
        scheduleId?.let { scheduleVM.getScheduleById(it) }
    }

    // PREFILL
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

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (scheduleId == null) "Add Schedule" else "Edit Schedule") },
                navigationIcon = {
                    IconButton(onClick = { activity.finish() }) {
                        Icon(Icons.Default.ArrowBack, null)
                    }
                }
            )
        }
    ) { padding ->

        if (loading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {

                // 🔽 ROUTE DROPDOWN
                item {
                    DropdownField(
                        label = "Route",
                        value = routeName,
                        options = routes.map { it.name },
                        onSelect = { name ->
                            val route = routes.first { it.name == name }
                            routeId = route.routeId
                            routeName = route.name
                        }
                    )
                }

                // 🔽 VEHICLE DROPDOWN (ACTIVE ONLY)
                item {
                    val activeVehicles = vehicles.filter { it.isActive }

                    DropdownField(
                        label = "Vehicle",
                        value = vehicleNumber,
                        options = activeVehicles.map { it.vehicleNumber },
                        onSelect = { num ->
                            val vehicle = activeVehicles.first { it.vehicleNumber == num }
                            vehicleId = vehicle.vehicleId
                            vehicleNumber = vehicle.vehicleNumber
                        }
                    )
                }

                // 🔽 DRIVER DROPDOWN
                item {
                    DropdownField(
                        label = "Driver",
                        value = driverName,
                        options = drivers!!.map { it.fullname },
                        onSelect = { name ->
                            val driver = drivers!!.first {
                                it.fullname == name
                            }
                            driverId = driver.userId
                            driverName = name
                        }
                    )
                }

                item {
                    DropdownField(
                        label = "Day of Week",
                        value = dayOfWeek,
                        options = daysOfWeek,
                        onSelect = { selected ->
                            dayOfWeek = selected
                        }
                    )
                }


                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                showTimePicker(context) { time ->
                                    startTime = time
                                }
                            }
                    ) {
                        OutlinedTextField(
                            value = startTime,
                            onValueChange = {},
                            modifier = Modifier.fillMaxWidth(),
                            label = { Text("Start Time") },
                            enabled = false,   // 🔴 IMPORTANT
                            readOnly = true
                        )
                    }
                }



                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                showTimePicker(context) { time ->
                                    endTime = time
                                }
                            }
                    ) {
                        OutlinedTextField(
                            value = endTime,
                            onValueChange = {},
                            modifier = Modifier.fillMaxWidth(),
                            label = { Text("End Time") },
                            enabled = false,   // 🔴 IMPORTANT
                            readOnly = true
                        )
                    }
                }



                item {
                    Button(
                        modifier = Modifier.fillMaxWidth().height(50.dp),
                        onClick = {

                            if (
                                routeId.isBlank() ||
                                vehicleId.isBlank() ||
                                driverId.isBlank() ||
                                dayOfWeek.isBlank() ||
                                startTime.isBlank() ||
                                endTime.isBlank()
                            ) {
                                Toast.makeText(context, "Fill all fields", Toast.LENGTH_SHORT).show()
                                return@Button
                            }

                            if (!isEndTimeAfterStart(startTime, endTime)) {
                                Toast.makeText(
                                    context,
                                    "End time must be after start time",
                                    Toast.LENGTH_SHORT
                                ).show()
                                return@Button
                            }


                            val model = ScheduleModel(
                                scheduleId = scheduleId ?: "",
                                routeId = routeId,
                                routeName = routeName,
                                driverId = driverId,
                                vehicleId = vehicleId,
                                vehicleNumber = vehicleNumber,
                                dayOfWeek = dayOfWeek,
                                startTime = startTime,
                                endTime = endTime
                            )

                            val conflict = scheduleVM.hasScheduleConflict(model, schedules)

                            if (conflict.first) {
                                Toast.makeText(context, conflict.second, Toast.LENGTH_LONG).show()
                                return@Button
                            }


                            if (scheduleId == null) {
                                scheduleVM.addSchedule(model) { success, msg ->
                                    Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                                    if (success) activity.finish()
                                }
                            } else {
                                scheduleVM.updateSchedule(model) { success, msg ->
                                    Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                                    if (success) activity.finish()
                                }
                            }
                        }
                    ) {
                        Text(if (scheduleId == null) "Save Schedule" else "Update Schedule")
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DropdownField(
    label: String,
    value: String,
    options: List<String>,
    onSelect: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded }
    ) {
        OutlinedTextField(
            value = value,
            onValueChange = {},
            readOnly = true,
            label = { Text(label) },
            trailingIcon = {
                ExposedDropdownMenuDefaults.TrailingIcon(expanded)
            },
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor()
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            options.forEach {
                DropdownMenuItem(
                    text = { Text(it) },
                    onClick = {
                        onSelect(it)
                        expanded = false
                    }
                )
            }
        }
    }
}

fun showTimePicker(
    context: android.content.Context,
    onTimeSelected: (String) -> Unit
) {
    val calendar = Calendar.getInstance()
    val hour = calendar.get(Calendar.HOUR_OF_DAY)
    val minute = calendar.get(Calendar.MINUTE)

    TimePickerDialog(
        context,
        { _, h, m ->
            val time = String.format("%02d:%02d", h, m)
            onTimeSelected(time)
        },
        hour,
        minute,
        true
    ).show()
}

fun isEndTimeAfterStart(startTime: String, endTime: String): Boolean {
    return try {
        val start = startTime.split(":")
        val end = endTime.split(":")

        val startMinutes = start[0].toInt() * 60 + start[1].toInt()
        val endMinutes = end[0].toInt() * 60 + end[1].toInt()

        endMinutes > startMinutes
    } catch (e: Exception) {
        false
    }
}
