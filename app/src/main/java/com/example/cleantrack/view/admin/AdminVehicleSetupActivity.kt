package com.example.cleantrack.view.admin

import android.app.Activity
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.cleantrack.model.VehicleModel
import com.example.cleantrack.repository.VehicleRepoImpl
import com.example.cleantrack.ui.theme.PrimaryGreen
import com.example.cleantrack.viewmodel.VehicleViewModel
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType

class AdminVehicleSetupActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val vehicleId = intent.getStringExtra("VEHICLE_ID")
        setContent {
            AdminVehicleSetupScreen(vehicleId)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminVehicleSetupScreen(vehicleId: String?) {
    val context = LocalContext.current
    val activity = context as Activity

    val vm = remember { VehicleViewModel(VehicleRepoImpl()) }
    val selectedVehicle by vm.vehicle.observeAsState(null)
    val loading by vm.loading.observeAsState(false)

    // UI STATE
    var vehicleNumber by remember { mutableStateOf("") }
    var type by remember { mutableStateOf("TRUCK") }
    var capacity by remember { mutableStateOf("") }
    var isActive by remember { mutableStateOf(true) }

    // For button loading state
    var isSaving by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }

    val vehicleTypes = listOf("TRUCK", "VAN", "BIKE")

    LaunchedEffect(vehicleId) {
        vehicleId?.let { vm.getVehicleById(it) }
    }

    LaunchedEffect(selectedVehicle) {
        selectedVehicle?.let {
            vehicleNumber = it.vehicleNumber
            type = it.type
            capacity = it.capacity
            isActive = it.active
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(PrimaryGreen, Color.White),
                    startY = 0f,
                    endY = 1000f
                )
            )
    ) {
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                CenterAlignedTopAppBar(
                    title = {
                        Text(
                            if (vehicleId == null) "Add Vehicle" else "Edit Vehicle",
                            style = TextStyle(color = Color.White, fontWeight = FontWeight.ExtraBold, fontSize = 20.sp)
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = { activity.finish() }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = Color.White)
                        }
                    },
                    actions = {
                        if (vehicleId != null) {
                            IconButton(onClick = { showDeleteDialog = true }) {
                                Icon(Icons.Default.Delete, null, tint = Color.White)
                            }
                        }
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.Transparent)
                )
            }
        ) { padding ->
            if (loading) {
                Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = PrimaryGreen)
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .padding(horizontal = 20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    contentPadding = PaddingValues(top = 10.dp, bottom = 40.dp)
                ) {
                    item {
                        OutlinedTextField(
                            value = vehicleNumber,
                            onValueChange = { vehicleNumber = it },
                            modifier = Modifier.testTag("vehicle_number_field").fillMaxWidth(),
                            label = { Text("Vehicle Number") },
                            // Use Next to move to the next field
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = PrimaryGreen,
                                focusedLabelColor = PrimaryGreen
                            )
                        )
                    }

                    item {
                        var expanded by remember { mutableStateOf(false) }
                        ExposedDropdownMenuBox(
                            expanded = expanded,
                            onExpandedChange = { expanded = !expanded }
                        ) {
                            OutlinedTextField(
                                value = type,
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Vehicle Type") },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
                                modifier = Modifier.fillMaxWidth().menuAnchor().testTag("type_dropdown"),
                                shape = RoundedCornerShape(12.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = PrimaryGreen,
                                    focusedLabelColor = PrimaryGreen
                                )
                            )
                            ExposedDropdownMenu(
                                expanded = expanded,
                                onDismissRequest = { expanded = false }
                            ) {
                                vehicleTypes.forEach {
                                    DropdownMenuItem(
                                        text = { Text(it) },
                                        onClick = {
                                            type = it
                                            expanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }

                    item {
                        OutlinedTextField(
                            value = capacity,
                            onValueChange = { capacity = it },
                            modifier = Modifier.testTag("capacity_field").fillMaxWidth(),
                            label = { Text("Capacity (kg)") },
                            // Use Done for the final text field
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Number,
                                imeAction = ImeAction.Done
                            ),
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = PrimaryGreen,
                                focusedLabelColor = PrimaryGreen
                            )
                        )
                    }

                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.5f))
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp).fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Vehicle Active Status", fontWeight = FontWeight.Medium)
                                Switch(
                                    checked = isActive,
                                    modifier = Modifier.testTag("status_switch"),
                                    onCheckedChange = { isActive = it },
                                    colors = SwitchDefaults.colors(checkedThumbColor = PrimaryGreen)
                                )
                            }
                        }
                    }

                    item {
                        Spacer(Modifier.height(10.dp))
                        Button(
                            modifier = Modifier.fillMaxWidth().height(56.dp).testTag("save_vehicle_button"),
                            enabled = !isSaving,
                            onClick = {
                                if (vehicleNumber.isBlank() || capacity.isBlank()) {
                                    Toast.makeText(context, "Please fill all fields", Toast.LENGTH_SHORT).show()
                                    return@Button
                                }

                                isSaving = true
                                val model = VehicleModel(
                                    vehicleId = vehicleId ?: "",
                                    vehicleNumber = vehicleNumber,
                                    type = type,
                                    capacity = capacity,
                                    active = isActive
                                )

                                val callback: (Boolean, String) -> Unit = { success, msg ->
                                    isSaving = false
                                    Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                                    if (success) activity.finish()
                                }

                                if (vehicleId == null) vm.addVehicle(model, callback)
                                else vm.updateVehicle(model, callback)
                            },
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen)
                        ) {
                            if (isSaving) {
                                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
                            } else {
                                Text(
                                    if (vehicleId == null) "Save Vehicle" else "Update Vehicle",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            shape = RoundedCornerShape(20.dp),
            title = { Text("Delete Vehicle", fontWeight = FontWeight.Bold) },
            text = { Text("Are you sure you want to delete this vehicle? This action cannot be undone.") },
            confirmButton = {
                TextButton(onClick = {
                    vm.deleteVehicle(vehicleId!!) { success, msg ->
                        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                        if (success) activity.finish()
                    }
                },modifier = Modifier.testTag("delete_vehicle_icon")) {
                    Text("Delete", color = Color.Red, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancel", color = Color.Black)
                }
            }
        )
    }
}