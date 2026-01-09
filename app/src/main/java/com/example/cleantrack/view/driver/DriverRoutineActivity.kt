package com.example.cleantrack.view.driver

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.cleantrack.model.ScheduleModel   // ✅ CORRECT IMPORT
import com.example.cleantrack.repository.ActiveTripRepoImpl
import com.example.cleantrack.repository.BinCollectionRepoImpl
import com.example.cleantrack.repository.BinRepoImpl
import com.example.cleantrack.repository.PointsRepoImpl
import com.example.cleantrack.repository.ScheduleRepoImpl
import com.example.cleantrack.repository.UserRepoImpl
import com.example.cleantrack.util.AppUtil
import com.example.cleantrack.viewmodel.ActiveTripViewModel
import com.example.cleantrack.viewmodel.ScheduleViewModel

class DriverRoutineActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // TODO replace with logged-in driver id
        val driverId = "DRIVER_ID_HERE"

        setContent {
            val scheduleViewModel = remember {
                ScheduleViewModel(ScheduleRepoImpl())
            }

            DriverRoutineScreen(
                driverId = driverId,
                viewModel = scheduleViewModel
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DriverRoutineScreen(
    driverId: String,
    viewModel: ScheduleViewModel
) {
    // ✅ EXPLICIT TYPE
    val schedules by viewModel.schedules.observeAsState()
    val safeSchedules = schedules ?: emptyList()


    val activeTripViewModel = remember {
        ActiveTripViewModel(ActiveTripRepoImpl(), UserRepoImpl(), BinRepoImpl(),
            BinCollectionRepoImpl(), PointsRepoImpl()
        )
    }

    val context = LocalContext.current

    LaunchedEffect(driverId) {
        viewModel.loadDriverSchedules(driverId)
    }

    // ✅ SAFE GROUPING
    val groupedSchedules: Map<String, List<ScheduleModel>> =
        safeSchedules
            .sortedBy { it.startTime }
            .groupBy { it.dayOfWeek }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("My Routine") }
            )
        }
    ) { padding ->

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {

            groupedSchedules.entries.forEach { entry ->

                val day = entry.key
                val daySchedules = entry.value

                // Day header
                item {
                    Text(
                        text = day,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(16.dp)
                    )
                }

                // Schedule list
                items(daySchedules) { schedule ->
                    ScheduleCard(
                        schedule = schedule
                    )
                }
            }
        }
    }
}

@Composable
fun ScheduleCard(
    schedule: ScheduleModel
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF2E7D32)
        )
    ) {
        Column(Modifier.padding(16.dp)) {

            Text(
                schedule.routeName,
                color = Color.White,
                fontWeight = FontWeight.Bold
            )

            Text(
                "Vehicle: ${schedule.vehicleNumber}",
                color = Color.White
            )

            Text(
                "${schedule.startTime} - ${schedule.endTime}",
                color = Color.White
            )
        }
    }
}
