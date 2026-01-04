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
import com.example.cleantrack.repository.ActiveTripRepoImpl
import com.example.cleantrack.repository.BinCollectionRepoImpl
import com.example.cleantrack.repository.BinRepoImpl
import com.example.cleantrack.repository.UserRepoImpl
import com.example.cleantrack.viewmodel.ActiveTripViewModel
import com.example.cleantrack.viewmodel.BinViewModel
import com.example.cleantrack.viewmodel.UserViewModel

class BinCollectionActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binId = intent.getStringExtra("BIN_ID") ?: ""

        setContent {
            BinCollectionScreen(binId) { finish() }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BinCollectionScreen(binId: String, onComplete: () -> Unit) {
    val context = androidx.compose.ui.platform.LocalContext.current

    // 🔹 Use BinViewModel to fetch bin details
    val binVM = remember { BinViewModel(BinRepoImpl()) }
    val collectionRepo = remember { BinCollectionRepoImpl() }
    val userViewModel  = remember { UserViewModel(UserRepoImpl()) }
    val activeTripViewModel = remember { ActiveTripViewModel(ActiveTripRepoImpl()) }


    val binDetails by binVM.bin.observeAsState()
    val loading by binVM.loading.observeAsState(false)

    // Form States
    var rating by remember { mutableIntStateOf(0) }
    var remarks by remember { mutableStateOf("") }
    var isSegregated by remember { mutableStateOf(true) }
    var isSaving by remember { mutableStateOf(false) }

    // Fetch data when activity starts
    LaunchedEffect(binId) {
        binVM.getBinById(binId)
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
                // --- Bin Header ---
                binDetails?.let { bin ->
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(Modifier.padding(16.dp)) {
                            Text(text = bin.label, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                            Text(text = "Category: ${bin.category}", color = Color.Gray)
                        }
                    }
                }

                // --- Segregation Status ---
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Was waste segregated correctly?", modifier = Modifier.weight(1f))
                    Switch(checked = isSegregated, onCheckedChange = { isSegregated = it })
                }

                // --- Rating System ---
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

                // --- Remarks ---
                OutlinedTextField(
                    value = remarks,
                    onValueChange = { remarks = it },
                    label = { Text("Optional Remarks") },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("e.g. Bin was overfilled") }
                )

                Spacer(Modifier.weight(1f))

                // --- Save Button ---
                Button(
                    onClick = {
                        isSaving = true
                        val model = BinCollectionModel(
                            binId = binId,
                            userId = binDetails?.ownerUserId ?: "",
                            driverId = "", // TODO: Get from shared preferences or auth
                            tripId = "CURRENT_TRIP_ID",     // TODO: Pass from previous activity
                            rating = rating,
                            remarks = remarks,
                            segregatedCorrectly = isSegregated,
                            pointsAwarded = if (isSegregated) 10 else 2
                        )

                        collectionRepo.addBinCollection(model) { success, msg ->
                            isSaving = false
                            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                            if (success) onComplete()
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isSaving && binDetails != null && rating > 0
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