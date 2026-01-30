package com.example.cleantrack.view.driver

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.cleantrack.model.BinCollectionModel
import com.example.cleantrack.model.NotificationPayload
import com.example.cleantrack.repository.*
import com.example.cleantrack.ui.theme.*
import com.example.cleantrack.viewmodel.ActiveTripViewModel
import com.example.cleantrack.viewmodel.BinViewModel
import com.example.cleantrack.viewmodel.NotificationViewModel
import com.example.cleantrack.viewmodel.UserViewModel
import kotlinx.coroutines.launch

class BinCollectionActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val binId = intent.getStringExtra("BIN_ID") ?: ""
        val tripId = intent.getStringExtra("TRIP_ID") ?: ""
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

    val binVM = remember { BinViewModel(BinRepoImpl()) }
    val userViewModel = remember { UserViewModel(UserRepoImpl(), BinCollectionRepoImpl()) }
    val activeTripViewModel = remember {
        ActiveTripViewModel(ActiveTripRepoImpl(),
            UserRepoImpl(),
            BinRepoImpl(),
            BinCollectionRepoImpl(),
            PointsRepoImpl())
    }
    val notificationViewModel = remember { NotificationViewModel(NotificationRepoImpl(), UserRepoImpl()) }

    val binDetails by binVM.bin.observeAsState()

    var currentDriverId by remember { mutableStateOf("") }
    var rating by remember { mutableIntStateOf(0) }
    var weightInput by remember { mutableStateOf("") }
    var remarks by remember { mutableStateOf("") }
    var isSegregated by remember { mutableStateOf(true) }
    var isSaving by remember { mutableStateOf(false) }

    val aiRepo = remember { AIRepository() }
    val scope = rememberCoroutineScope()

    LaunchedEffect(binId) {
        binVM.getBinById(binId)
        currentDriverId = userViewModel.getCurrentUserId() ?: ""
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(Blue, Green, Color.White),
                    startY = 0f,
                    endY = 1500f
                )
            )
    ) {
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                CenterAlignedTopAppBar(
                    title = { Text("Log Bin Collection", fontWeight = FontWeight.ExtraBold, color = Color.White) },
                    navigationIcon = {
                        IconButton(onClick = onComplete) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                        }
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.Transparent)
                )
            }
        ) { pad ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(pad)
                    .padding(horizontal = 20.dp)
                    // Added testTag to the scrollable column for robust testing
                    .testTag("collection_scroll_column")
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Spacer(modifier = Modifier.height(10.dp))

                // Header (Bin details Card)
                binDetails?.let { bin ->
                    Card(
                        modifier = Modifier.fillMaxWidth().testTag("bin_details_card"),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(4.dp)
                    ) {
                        Column(Modifier.padding(20.dp)) {
                            Text(text = bin.label, fontSize = 22.sp, fontWeight = FontWeight.ExtraBold, color = Black)
                            Text(text = "Category: ${bin.category}", color = Color.Gray, fontSize = 14.sp)
                            Badge(containerColor = Green.copy(alpha = 0.1f), contentColor = Green, modifier = Modifier.padding(top = 8.dp)) {
                                Text(text = "Route: $actualRouteName", modifier = Modifier.padding(4.dp))
                            }
                        }
                    }
                }

                // Input Form Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(4.dp)
                ) {
                    Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("Waste Segregated?", fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                            Switch(
                                checked = isSegregated,
                                onCheckedChange = { isSegregated = it },
                                // Added testTag for segregation switch
                                modifier = Modifier.testTag("segregation_switch"),
                                colors = SwitchDefaults.colors(checkedThumbColor = Green, checkedTrackColor = Green.copy(0.3f))
                            )
                        }

                        Divider(thickness = 0.5.dp, color = Color.LightGray)

                        Text("Rate Performance:", fontWeight = FontWeight.Bold)
                        Row(horizontalArrangement = Arrangement.Center, modifier = Modifier.fillMaxWidth()) {
                            repeat(5) { index ->
                                val starIndex = index + 1
                                Icon(
                                    imageVector = if (rating >= starIndex) Icons.Default.Star else Icons.Default.StarBorder,
                                    contentDescription = null,
                                    tint = if (rating >= starIndex) Color(0xFFFFB300) else Color.Gray,
                                    modifier = Modifier
                                        .size(45.dp)
                                        // Dynamic testTag for each star rating (star_1, star_2, etc.)
                                        .testTag("star_$starIndex")
                                        .clickable { rating = starIndex }
                                )
                            }
                        }

                        OutlinedTextField(
                            value = weightInput,
                            onValueChange = { if (it.isEmpty() || it.toDoubleOrNull() != null) weightInput = it },
                            label = { Text("Weight (kg)") },
                            // Added testTag for weight input
                            modifier = Modifier.fillMaxWidth().testTag("weight_input"),
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Green, focusedLabelColor = Green),
                            keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                                keyboardType = androidx.compose.ui.text.input.KeyboardType.Decimal
                            )
                        )

                        OutlinedTextField(
                            value = remarks,
                            onValueChange = { remarks = it },
                            label = { Text("Remarks") },
                            // Added testTag for remarks input
                            modifier = Modifier.fillMaxWidth().testTag("remarks_input"),
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Green, focusedLabelColor = Green)
                        )
                    }
                }

                Spacer(Modifier.height(20.dp))

                Button(
                    onClick = {
                        val bin = binDetails
                        val weight = weightInput.toDoubleOrNull() ?: 0.0
                        if (bin != null && rating > 0 && weight > 0) {
                            isSaving = true
                            scope.launch {
                                val temporaryHistoryList = listOf(
                                    BinCollectionModel(rating = rating, weight = weight, segregatedCorrectly = isSegregated, remarks = remarks)
                                )
                                val aiFeedback = aiRepo.generateGlobalOverview(temporaryHistoryList)

                                activeTripViewModel.collectBinWithAI(
                                    bin = bin,
                                    driverId = currentDriverId,
                                    tripId = tripId,
                                    rating = rating,
                                    weight = weight,
                                    remarks = remarks,
                                    aiTip = aiFeedback,
                                    isSegregated = isSegregated
                                ) { success, msg ->
                                    if (success) {
                                        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                                        aiRepo.saveProcessedInsight(
                                            collection = BinCollectionModel(tripId = tripId, rating = rating, weight = weight, segregatedCorrectly = isSegregated),
                                            aiReview = aiFeedback,
                                            routeId = routeId,
                                            routeName = actualRouteName,
                                            ownerId = bin.ownerUserId
                                        )
                                        notificationViewModel.notifyUser(
                                            bin.ownerUserId,
                                            NotificationPayload(title = "Bin Collected!", message = "AI Feedback: $aiFeedback", type = "bin_rating", actionType = "bin_rating")
                                        )
                                        isSaving = false
                                        onComplete()
                                    } else {
                                        isSaving = false
                                        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                                    }
                                }
                            }
                        } else if (weight <= 0) {
                            Toast.makeText(context, "Please enter a valid weight", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(context, "Please provide a rating", Toast.LENGTH_SHORT).show()
                        }
                    },
                    // Added testTag for the primary action button
                    modifier = Modifier.fillMaxWidth().height(56.dp).testTag("save_log_button"),
                    shape = RoundedCornerShape(15.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Green)
                ) {
                    if (isSaving) {
                        CircularProgressIndicator(
                            // Added testTag for the saving state indicator
                            modifier = Modifier.size(24.dp).testTag("save_progress"),
                            color = Color.White,
                            strokeWidth = 3.dp
                        )
                    } else {
                        Text("Save Collection Log", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }
                }
                Spacer(Modifier.height(24.dp))
            }
        }
    }
}