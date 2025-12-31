package com.example.cleantrack.view.user

import android.app.Activity
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.cleantrack.model.ScheduleModel
import com.example.cleantrack.repository.ScheduleRepoImpl
import com.example.cleantrack.repository.UserRepoImpl
import com.example.cleantrack.ui.theme.Black
import com.example.cleantrack.ui.theme.White
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

    // Initializing ViewModels with Repositories
    val scheduleVM = remember { ScheduleViewModel(ScheduleRepoImpl()) }
    val userVM = remember { UserViewModel(UserRepoImpl()) }

    // Observing State
    val allSchedules by scheduleVM.schedules.observeAsState(emptyList())
    val userProfile by userVM.user.observeAsState()
    val loading by scheduleVM.loading.observeAsState(false)

    // Fetch data when screen opens
    LaunchedEffect(Unit) {
        scheduleVM.getAllSchedules()
        userVM.getCurrentUserId()?.let { userVM.getUserById(it) }
    }

    // Filter schedules that match user's activeRouteId
    val mySchedules = remember(allSchedules, userProfile) {
        allSchedules?.filter {
            it.routeId == userProfile?.activeRouteId && it.active
        } ?: emptyList()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("My Collection Schedules", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { (context as? Activity)?.finish() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = White)
            )
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding).background(White)) {
            when {
                loading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
                userProfile?.activeRouteId.isNullOrEmpty() -> {
                    // State: User has not selected a route yet
                    EmptyStateView(
                        title = "No Route Selected",
                        description = "Please go to 'Live Tracking' and confirm your neighborhood route to see when the truck arrives."
                    )
                }
                mySchedules.isEmpty() -> {
                    // State: Route selected but no schedules added by Admin for it
                    EmptyStateView(
                        title = "No Upcoming Pickups",
                        description = "There are currently no active schedules for ${userProfile?.activeRouteId}. Check back later!"
                    )
                }
                else -> {
                    // State: Success, show the list
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(mySchedules) { schedule ->
                            UserScheduleCard(schedule)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun UserScheduleCard(schedule: ScheduleModel) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = White),
        elevation = CardDefaults.cardElevation(2.dp),
        border = BorderStroke(1.dp, Color.LightGray.copy(alpha = 0.5f))
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Default.Schedule,
                contentDescription = null,
                tint = Color(0xFF4CAF50),
                modifier = Modifier.size(32.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = schedule.routeName,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
                Text(
                    text = "${schedule.dayOfWeek} | ${schedule.startTime} - ${schedule.endTime}",
                    color = Color.Gray,
                    fontSize = 14.sp
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "Truck",
                    fontSize = 10.sp,
                    color = Color.Gray
                )
                Text(
                    text = schedule.vehicleNumber,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
            }
        }
    }
}

@Composable
fun EmptyStateView(title: String, description: String) {
    Column(
        modifier = Modifier.fillMaxSize().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(title, fontWeight = FontWeight.Bold, fontSize = 20.sp, color = Black)
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            description,
            textAlign = TextAlign.Center,
            color = Color.Gray,
            fontSize = 14.sp
        )
    }
}