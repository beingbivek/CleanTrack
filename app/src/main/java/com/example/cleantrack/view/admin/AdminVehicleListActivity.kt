package com.example.cleantrack.view.admin

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.cleantrack.model.VehicleModel
import com.example.cleantrack.repository.VehicleRepoImpl
import com.example.cleantrack.view.admin.ui.theme.CleanTrackTheme
import com.example.cleantrack.viewmodel.VehicleViewModel

class AdminVehicleListActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AdminVehicleListScreen()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminVehicleListScreen() {

    val vm = remember {
        VehicleViewModel(VehicleRepoImpl())
    }

    val vehicles by vm.vehicles.observeAsState(emptyList())
    val loading by vm.loading.observeAsState(false)
    val context = LocalContext.current

    var showDeleteDialog by remember { mutableStateOf(false) }
    var selectedVehicleId by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        vm.getAllVehicles()
    }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Manage Vehicles") })
        },
        floatingActionButton = {
            FloatingActionButton(onClick = {
                context.startActivity(
                    Intent(context, AdminVehicleSetupActivity::class.java)
                )
            }) {
                Icon(Icons.Default.Add, null)
            }
        }
    ) { padding ->

        if (loading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {

                items( vehicles ?: emptyList()) { vehicle ->
                    VehicleAdminCard(
                        vehicle = vehicle,
                        onEdit = {
                            val intent =
                                Intent(context, AdminVehicleSetupActivity::class.java)
                            intent.putExtra("VEHICLE_ID", vehicle.vehicleId)
                            context.startActivity(intent)
                        },
                        onDelete = {
                            selectedVehicleId = vehicle.vehicleId
                            showDeleteDialog = true
                        }
                    )
                }
            }
        }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete Vehicle") },
            text = { Text("Are you sure you want to delete this vehicle?") },
            confirmButton = {
                Button(onClick = {
                    vm.deleteVehicle(selectedVehicleId) { success, msg ->
                        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                        if (success) {
                            vm.getAllVehicles()
                            showDeleteDialog = false
                        }
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

@Composable
fun VehicleAdminCard(
    vehicle: VehicleModel,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(12.dp),
        elevation = CardDefaults.cardElevation(6.dp)
    ) {
        Column(Modifier.padding(16.dp)) {

            Text(vehicle.vehicleNumber, fontWeight = FontWeight.Bold)
            Text("Type: ${vehicle.type}")
            Text("Capacity: ${vehicle.capacity}")
            Text("Status: ${vehicle.isActive}")

            Spacer(Modifier.height(10.dp))

            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                IconButton(onClick = onEdit) {
                    Icon(Icons.Default.Edit, null)
                }
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, tint = Color.Red, contentDescription = null)
                }
            }
        }
    }
}

