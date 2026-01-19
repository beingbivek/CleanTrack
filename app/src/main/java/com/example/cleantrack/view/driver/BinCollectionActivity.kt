package com.example.cleantrack.view.driver

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.cleantrack.model.BinCollectionModel
import com.example.cleantrack.model.NotificationPayload
import com.example.cleantrack.repository.AIRepository
import com.example.cleantrack.repository.ActiveTripRepoImpl
import com.example.cleantrack.repository.BinCollectionRepoImpl
import com.example.cleantrack.repository.BinRepoImpl
import com.example.cleantrack.repository.NotificationRepoImpl
import com.example.cleantrack.repository.UserRepoImpl
import com.example.cleantrack.viewmodel.ActiveTripViewModel
import com.example.cleantrack.viewmodel.BinViewModel
import com.example.cleantrack.viewmodel.NotificationViewModel
import com.example.cleantrack.viewmodel.UserViewModel
import kotlinx.coroutines.launch

class BinCollectionActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binId = intent.getStringExtra("BIN_ID") ?: ""
        val tripId = intent.getStringExtra("TRIP_ID") ?: "" // 🔹 FETCH TRIP_ID FROM INTENT
        val actualRouteName = intent.getStringExtra("ROUTE_NAME") ?: ""
        val routeId = intent.getStringExtra("ROUTE_ID") ?: ""

        if (actualRouteName.isBlank() || routeId.isBlank()) {
            Toast.makeText(this, "Route info missing", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        setContent {
            BinCollectionScreen(
                binId = binId,
                tripId = tripId,
                routeId = routeId,
                actualRouteName = actualRouteName
            ) {
                finish()
            }
        }
    }
}
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BinCollectionScreen(binId: String, tripId: String, routeId: String, actualRouteName: String, onComplete: () -> Unit) {
    val context = androidx.compose.ui.platform.LocalContext.current

    // ViewModels
    val binVM = remember { BinViewModel(BinRepoImpl()) }
    val userViewModel = remember {
        UserViewModel(
            UserRepoImpl(),
            BinCollectionRepoImpl() // 🔹 Add this second parameter
        )
    }

    // Inject all required Repositories into the ActiveTripViewModel
    val activeTripViewModel = remember {
        ActiveTripViewModel(
            ActiveTripRepoImpl(),
            UserRepoImpl(),
            BinRepoImpl(),
            BinCollectionRepoImpl(),
            com.example.cleantrack.repository.PointsRepoImpl() // Add this!
        )
    }
    val notificationViewModel = remember { NotificationViewModel(NotificationRepoImpl(), UserRepoImpl()) }

    val binDetails by binVM.bin.observeAsState()
    val loading by binVM.loading.observeAsState(false)

    var currentDriverId by remember { mutableStateOf("") }
    var rating by remember { mutableIntStateOf(0) }
    var remarks by remember { mutableStateOf("") }
    var isSegregated by remember { mutableStateOf(true) }
    var isSaving by remember { mutableStateOf(false) }

    val aiRepo = remember { AIRepository() } // Initialize AI Repo
    val scope = rememberCoroutineScope()

    LaunchedEffect(binId) {
        binVM.getBinById(binId)
        currentDriverId = userViewModel.getCurrentUserId() ?: ""
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Log Bin Collection") }) }
    ) { pad ->
        if (loading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(pad)
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Header (Bin details)
                binDetails?.let { bin ->
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(Modifier.padding(16.dp)) {
                            Text(text = bin.label, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                            Text(text = "Category: ${bin.category}", color = Color.Gray)
                        }
                    }
                }

                // Segregation Switch
                Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Text("Was waste segregated correctly?", modifier = Modifier.weight(1f))
                    Switch(checked = isSegregated, onCheckedChange = { isSegregated = it })
                }

                // Star Rating
                Text("Rate User Performance:")
                Row {
                    repeat(5) { index ->
                        val starIndex = index + 1
                        Icon(
                            imageVector = if (rating >= starIndex) Icons.Default.Star else Icons.Default.StarBorder,
                            contentDescription = null,
                            tint = if (rating >= starIndex) Color(0xFFFFB300) else Color.Gray,
                            modifier = Modifier
                                .size(40.dp)
                                .clickable { rating = starIndex }
                        )
                    }
                }

                // Remarks
                OutlinedTextField(
                    value = remarks,
                    onValueChange = { remarks = it },
                    label = { Text("Optional Remarks") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(Modifier.weight(1f))

                // --- NEW SAVE LOGIC ---
                // Inside BinCollectionScreen (the UI where the driver rates)
                Button(
                    onClick = {
                        val bin = binDetails
                        if (bin != null && rating > 0) { // Ensure rating is selected
                            isSaving = true
                            scope.launch {
                                // 1. Generate an AI Review/Tip for this specific scan
                                // We use generateGlobalOverview as a base to create the feedback
                                val temporaryHistoryList = listOf(
                                    BinCollectionModel(
                                        rating = rating,
                                        segregatedCorrectly = isSegregated,
                                        remarks = remarks
                                    )
                                )

                                val aiFeedback = aiRepo.generateGlobalOverview(temporaryHistoryList)

                                // 2. Save the regular collection log
                                activeTripViewModel.collectBinWithAI(
                                    bin = bin,
                                    driverId = currentDriverId,
                                    tripId = tripId,
                                    rating = rating,
                                    remarks = remarks,
                                    aiTip = aiFeedback, // Store AI's thought in the collection itself
                                    isSegregated = isSegregated
                                ) { success, msg ->
                                    if (success) {
                                        // 3. IMPORTANT: Feed the Admin's Route Strategy!
                                        // This saves to the "RouteInsights" table we created
                                        aiRepo.saveProcessedInsight(
                                            collection = BinCollectionModel(
                                                tripId = tripId,
                                                rating = rating,
                                                segregatedCorrectly = isSegregated
                                            ),
                                            aiReview = aiFeedback,
                                            routeId = routeId,
                                            routeName = actualRouteName
                                        )

                                        notificationViewModel.notifyUser(
                                            bin.ownerUserId,
                                            NotificationPayload(
                                                title = "Bin Collected!",
                                                message = "AI Feedback: $aiFeedback",
                                                type = "bin_rating",
                                                actionType = "bin_rating"
                                            )
                                        )
                                        isSaving = false
                                        onComplete()
                                    } else {
                                        isSaving = false
                                        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                                    }
                                }
                            }
                        } else {
                            Toast.makeText(context, "Please provide a rating", Toast.LENGTH_SHORT).show()
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    if (isSaving) {
                        CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White)
                    } else {
                        Text("Save Collection Log")
                    }
                }
            }
        }
    }
}