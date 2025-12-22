package com.example.cleantrack.view.admin

import android.app.Activity
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.cleantrack.model.VehicleModel
import com.example.cleantrack.repository.VehicleRepoImpl
import com.example.cleantrack.viewmodel.VehicleViewModel

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

    val vm = remember {
        VehicleViewModel(VehicleRepoImpl())
    }

    val selectedVehicle by vm.vehicle.observeAsState(null)
    val loading by vm.loading.observeAsState(false)

    var showDeleteDialog by remember { mutableStateOf(false) }

    // ✅ UI STATE (MATCHES MODEL)
    var vehicleNumber by remember { mutableStateOf("") }
    var type by remember { mutableStateOf("TRUCK") }
    var capacity by remember { mutableStateOf("") }
    var isActive by remember { mutableStateOf(true) }

    val vehicleTypes = listOf("TRUCK", "VAN", "BIKE")

    // LOAD FOR EDIT
    LaunchedEffect(vehicleId) {
        vehicleId?.let { vm.getVehicleById(it) }
    }

    // PREFILL
    LaunchedEffect(selectedVehicle) {
        selectedVehicle?.let {
            vehicleNumber = it.vehicleNumber
            type = it.type
            capacity = it.capacity
            isActive = it.isActive
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (vehicleId == null) "Add Vehicle" else "Edit Vehicle") },
                navigationIcon = {
                    IconButton(onClick = { activity.finish() }) {
                        Icon(Icons.Default.ArrowBackIosNew, null)
                    }
                },
                actions = {
                    if (vehicleId != null) {
                        IconButton(onClick = { showDeleteDialog = true }) {
                            Icon(Icons.Default.Delete, null)
                        }
                    }
                }
            )
        }
    ) { padding ->

        if (loading) {
            Box(
                Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {

                item {
                    OutlinedTextField(
                        value = vehicleNumber,
                        onValueChange = { vehicleNumber = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Vehicle Number") }
                    )
                }

                item {
                    // TYPE DROPDOWN
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
                            trailingIcon = {
                                ExposedDropdownMenuDefaults.TrailingIcon(expanded)
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor()
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
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Capacity") }
                    )
                }

                item {
                    // ACTIVE SWITCH
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Vehicle Active")
                        Switch(
                            checked = isActive,
                            onCheckedChange = { isActive = it }
                        )
                    }
                }

                item {
                    Button(
                        modifier = Modifier.fillMaxWidth().height(50.dp),
                        onClick = {

                            if (vehicleNumber.isBlank() || capacity.isBlank()) {
                                Toast.makeText(context, "Fill all fields", Toast.LENGTH_SHORT).show()
                                return@Button
                            }

                            val model = VehicleModel(
                                vehicleId = vehicleId ?: "",
                                vehicleNumber = vehicleNumber,
                                type = type,
                                capacity = capacity,
                                isActive = isActive
                            )

                            if (vehicleId == null) {
                                vm.addVehicle(model) { success, msg ->
                                    Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                                    if (success) activity.finish()
                                }
                            } else {
                                vm.updateVehicle(model) { success, msg ->
                                    Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                                    if (success) activity.finish()
                                }
                            }
                        }
                    ) {
                        Text(if (vehicleId == null) "Save Vehicle" else "Update Vehicle")
                    }
                }
            }
        }
    }

    // DELETE
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete Vehicle") },
            text = { Text("Are you sure you want to delete this vehicle?") },
            confirmButton = {
                Button(onClick = {
                    vm.deleteVehicle(vehicleId!!) { success, msg ->
                        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                        if (success) activity.finish()
                    }
                }) { Text("Delete") }
            },
            dismissButton = {
                Button(onClick = { showDeleteDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}
