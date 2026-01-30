package com.example.cleantrack.view.driver

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.cleantrack.model.TripHistoryUiModel
import com.example.cleantrack.repository.*
import com.example.cleantrack.ui.theme.*
import com.example.cleantrack.view.common.TripHistoryCard
import com.example.cleantrack.viewmodel.ActiveTripViewModel
import java.text.SimpleDateFormat
import java.util.*

class DriversTripHistoryActivity : ComponentActivity() {

    private lateinit var viewModel: ActiveTripViewModel

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        viewModel = ActiveTripViewModel(
            ActiveTripRepoImpl(),
            UserRepoImpl(),
            BinRepoImpl(),
            BinCollectionRepoImpl(),
            PointsRepoImpl()
        )

        val driverId = UserRepoImpl().getCurrentUserId() ?: ""
        if (driverId.isNotEmpty()) {
            viewModel.fetchDriverHistory(driverId)
        }

        setContent {
            val historyList by viewModel.tripHistory.observeAsState(emptyList())
            val isLoading by viewModel.loading.observeAsState(false)
            var selectedTrip by remember { mutableStateOf<TripHistoryUiModel?>(null) }

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
                    containerColor = Color.Transparent, // Key to show the gradient
                    topBar = {
                        CenterAlignedTopAppBar(
                            title = {
                                Text("My Trip History", fontWeight = FontWeight.ExtraBold, color = Color.White)
                            },
                            navigationIcon = {
                                IconButton(onClick = { finish() }) {
                                    Icon(
                                        Icons.AutoMirrored.Filled.ArrowBack,
                                        contentDescription = "Back",
                                        tint = Color.White
                                    )
                                }
                            },
                            colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                                containerColor = Color.Transparent
                            )
                        )
                    }
                ) { paddingValues ->
                    Box(modifier = Modifier.padding(paddingValues).fillMaxSize()) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.align(Alignment.Center),
                                color = PrimaryGreen
                            )
                        } else if (historyList.isEmpty()) {
                            Text(
                                "No trips found.",
                                color = Color.White.copy(0.8f),
                                modifier = Modifier.align(Alignment.Center)
                            )
                        } else {
                            LazyColumn(
                                modifier = Modifier.fillMaxSize(),
                                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                items(historyList) { item ->
                                    TripHistoryCard(item) {
                                        selectedTrip = item
                                    }
                                }
                            }
                        }

                        // Detailed Info Dialog
                        selectedTrip?.let { item ->
                            // Use startTimestamp to match your TripHistoryCard logic
                            val timeInMillis = item.trip.startTimestamp
                            val sdf = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault())
                            val dateString = sdf.format(Date(timeInMillis))

                            AlertDialog(
                                onDismissRequest = { selectedTrip = null },
                                shape = RoundedCornerShape(24.dp),
                                containerColor = Color.White,
                                confirmButton = {
                                    TextButton(onClick = { selectedTrip = null }) {
                                        Text("Close", color = Green, fontWeight = FontWeight.Bold)
                                    }
                                },
                                title = {
                                    Column {
                                        // Dark Gray Date matching Announcements Activity
                                        Text(
                                            text = dateString,
                                            fontSize = 11.sp,
                                            color = Color(0xFF424242),
                                            fontWeight = FontWeight.Bold
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = item.trip.routeName,
                                            fontWeight = FontWeight.ExtraBold,
                                            fontSize = 20.sp,
                                            color = Black
                                        )
                                    }
                                },
                                text = {
                                    Column {
                                        DetailRow("Driver Name", item.driverName)
                                        DetailRow("Vehicle No", item.trip.vehicleNumber)

                                        HorizontalDivider(
                                            modifier = Modifier.padding(vertical = 12.dp),
                                            thickness = 0.5.dp,
                                            color = Color.LightGray.copy(0.5f)
                                        )

                                        DetailRow("Total Bins", "${item.totalBins}")
                                        DetailRow("Collected", "${item.collectedBins}")

                                        val remaining = item.totalBins - item.collectedBins
                                        DetailRow("Remaining", "$remaining")

                                        DetailRow("Status", item.trip.status)
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DetailRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, color = Color.Gray, fontWeight = FontWeight.Medium, fontSize = 14.sp)
        Text(text = value, color = Black, fontWeight = FontWeight.Bold, fontSize = 14.sp)
    }
}