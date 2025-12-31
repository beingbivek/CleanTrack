package com.example.cleantrack.view.user

import android.app.Activity
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

    // --- State for Floating Detail Card ---
    var selectedScheduleForDetail by remember { mutableStateOf<ScheduleModel?>(null) }
    val sheetState = rememberModalBottomSheetState()
    var showSheet by remember { mutableStateOf(false) }

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
                    ) {items(mySchedules) { schedule ->
                        // PASS THE CLICK ACTION HERE
                        UserScheduleCard(
                            schedule = schedule,
                            onClick = {
                                selectedScheduleForDetail = schedule
                                showSheet = true
                            }
                        )
                    }
                        }
                    // --- THE FLOATING DETAIL CARD (ModalBottomSheet) ---
                    if (showSheet && selectedScheduleForDetail != null) {
                        ModalBottomSheet(
                            onDismissRequest = { showSheet = false },
                            sheetState = sheetState,
                            containerColor = White,
                            dragHandle = { BottomSheetDefaults.DragHandle() }
                        ) {
                            ScheduleDetailContent(selectedScheduleForDetail!!)
                        }
                    }
                }
            }
        }
        }
    }


@Composable
fun UserScheduleCard(schedule: ScheduleModel, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth()
            .clickable { onClick() },
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
fun ScheduleDetailContent(schedule: ScheduleModel) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 16.dp)
            .padding(bottom = 32.dp) // Extra padding for the bottom
    ) {
        Text(
            text = "Schedule Details",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = Black
        )
        Spacer(modifier = Modifier.height(16.dp))

        DetailRow(label = "Route Name", value = schedule.routeName)
        DetailRow(label = "Day of Week", value = schedule.dayOfWeek)
        DetailRow(label = "Time Slot", value = "${schedule.startTime} - ${schedule.endTime}")
        DetailRow(label = "Vehicle No", value = schedule.vehicleNumber)
        DetailRow(label = "Vehicle ID", value = schedule.vehicleId)
        DetailRow(label = "Driver ID", value = schedule.driverId)

        Spacer(modifier = Modifier.height(20.dp))

        // Status Badge
        Surface(
            color = if (schedule.active) Color(0xFFE8F5E9) else Color(0xFFFFEBEE),
            shape = MaterialTheme.shapes.small
        ) {
            Text(
                text = if (schedule.active) "STATUS: ACTIVE" else "STATUS: INACTIVE",
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                color = if (schedule.active) Color(0xFF2E7D32) else Color.Red,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun DetailRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, color = Color.Gray, fontWeight = FontWeight.Medium)
        Text(text = value, color = Black, fontWeight = FontWeight.SemiBold)
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