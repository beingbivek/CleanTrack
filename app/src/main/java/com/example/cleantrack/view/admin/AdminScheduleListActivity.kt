package com.example.cleantrack.view.admin

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.cleantrack.model.ScheduleModel
import com.example.cleantrack.repository.ScheduleRepoImpl
import com.example.cleantrack.repository.UserRepoImpl
import com.example.cleantrack.viewmodel.ScheduleViewModel
import com.example.cleantrack.viewmodel.UserViewModel
import kotlin.text.get

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

    val vm = remember {
        ScheduleViewModel(ScheduleRepoImpl())
    }

    val dVM = remember { UserViewModel(UserRepoImpl()) }
    dVM.getAllDrivers()
    val drivers by dVM.drivers.observeAsState(emptyList())

    val schedules by vm.schedules.observeAsState(emptyList())
    val loading by vm.loading.observeAsState(false)
    val context = LocalContext.current

    val driverMap = remember(drivers) {
        drivers!!.associateBy(
            keySelector = { it.userId },
            valueTransform = { it.fullname }
        )
    }


    var showDeleteDialog by remember { mutableStateOf(false) }
    var selectedScheduleId by remember { mutableStateOf("") }

    // SAME PATTERN AS ROUTES
    LaunchedEffect(Unit) {
        vm.getAllSchedules()
    }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Manage Schedules") })
        },
        floatingActionButton = {
            FloatingActionButton(onClick = {
                val intent =
                    Intent(context, AdminScheduleSetupActivity::class.java)
                context.startActivity(intent)
            }) {
                Icon(Icons.Default.Add, null)
            }
        }
    ) { padding ->

        if (loading) {
            Box(
                Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {

                // ✅ EXACT SAME items() USAGE AS ROUTES
                items(schedules ?: emptyList()) { schedule ->

                    ScheduleAdminCard(
                        schedule = schedule,
                        driverName = driverMap[schedule.driverId] ?: "Unknown",
                        onEdit = {
                            val intent =
                                Intent(context, AdminScheduleSetupActivity::class.java)
                            intent.putExtra("SCHEDULE_ID", schedule.scheduleId)
                            context.startActivity(intent)
                        },
                        onDelete = {
                            selectedScheduleId = schedule.scheduleId
                            showDeleteDialog = true
                        }
                    )
                }
            }
        }
    }

    // DELETE CONFIRMATION (same pattern)
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete Schedule") },
            text = { Text("Are you sure you want to delete this schedule?") },
            confirmButton = {
                Button(onClick = {
                    vm.deleteSchedule(selectedScheduleId) { success, msg ->
                        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                        if (success) {
                            vm.getAllSchedules()
                            showDeleteDialog = false
                        }
                    }
                }) {
                    Text("Delete")
                }
            },
            dismissButton = {
                Button(onClick = { showDeleteDialog = false }) {
                    Text("Cancel")
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
            .padding(12.dp),
        elevation = CardDefaults.cardElevation(6.dp)
    ) {
        Column(Modifier.padding(16.dp)) {

            Text(
                schedule.routeName,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )

            Spacer(Modifier.height(6.dp))

            Text("Driver: " + driverName)
            Text("Vehicle: ${schedule.vehicleNumber}")
            Text("${schedule.dayOfWeek} | ${schedule.startTime} - ${schedule.endTime}")

            Spacer(Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                IconButton(onClick = onEdit) {
                    Icon(Icons.Default.Edit, contentDescription = "Edit")
                }
                IconButton(onClick = onDelete) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "Delete",
                        tint = Color.Red
                    )
                }
            }
        }
    }
}
