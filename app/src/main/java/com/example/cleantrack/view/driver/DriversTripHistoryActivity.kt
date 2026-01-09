package com.example.cleantrack.view.driver

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.cleantrack.model.ActiveTripModel
import com.example.cleantrack.model.TripHistoryUiModel
import com.example.cleantrack.repository.*
import com.example.cleantrack.view.common.TripHistoryCard
import com.example.cleantrack.viewmodel.ActiveTripViewModel
import java.text.SimpleDateFormat
import java.util.*

class DriversTripHistoryActivity : ComponentActivity() {

    private lateinit var viewModel: ActiveTripViewModel

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize ViewModel with the required Repositories
        viewModel = ActiveTripViewModel(
            ActiveTripRepoImpl(),
            UserRepoImpl(),
            BinRepoImpl(),
            BinCollectionRepoImpl(),
            PointsRepoImpl()
        )

        // Start fetching data
        val driverId = UserRepoImpl().getCurrentUserId() ?: ""
        if (driverId.isNotEmpty()) {
            viewModel.fetchDriverHistory(driverId)
        }

        setContent {
            val historyList by viewModel.tripHistory.observeAsState(emptyList())
            val isLoading by viewModel.loading.observeAsState(false)
            var selectedTrip by remember { mutableStateOf<TripHistoryUiModel?>(null) }

            Scaffold(
                topBar = {
                    TopAppBar(
                        title = { Text("My Trip History") },
                        navigationIcon = {
                            IconButton(onClick = { finish() }) {
                                Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
                    )
                }
            ) { paddingValues ->
                Box(modifier = Modifier.padding(paddingValues).fillMaxSize()) {
                    if (isLoading) {
                        CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                    } else if (historyList.isEmpty()) {
                        Text("No trips found.", modifier = Modifier.align(Alignment.Center))
                    } else {
                        LazyColumn(modifier = Modifier.fillMaxSize()) {
                            items(historyList) { item ->
                                TripHistoryCard(item) {
                                    selectedTrip = item // Opens the details state
                                }
                            }
                        }
                    }

                    // Detailed Info Dialog
                    selectedTrip?.let { item ->
                        AlertDialog(
                            onDismissRequest = { selectedTrip = null },
                            confirmButton = {
                                TextButton(onClick = { selectedTrip = null }) { Text("Close") }
                            },
                            title = { Text(item.trip.routeName) },
                            text = {
                                Column {
                                    DetailRow("Vehicle No:", item.trip.vehicleNumber)
                                    DetailRow("Driver Name:", item.driverName)
                                    DetailRow("Total Bins:", "${item.totalBins}")
                                    DetailRow("Collected:", "${item.collectedBins}")
                                    DetailRow("Missed:", "${item.totalBins - item.collectedBins}")
                                    DetailRow("Status:", item.trip.status)
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun DetailRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
        Text(text = value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
    }
}

