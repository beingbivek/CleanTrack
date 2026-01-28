package com.example.cleantrack.view.admin

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Route
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.cleantrack.model.map.RouteModel
import com.example.cleantrack.repository.RouteRepoImpl
import com.example.cleantrack.ui.theme.PrimaryGreen // Use consistent theme color
import com.example.cleantrack.viewmodel.RouteViewModel

class AdminRouteListActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AdminRouteListScreen()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminRouteListScreen() {
    val vm = remember { RouteViewModel(RouteRepoImpl()) }
    val routes by vm.routes.observeAsState(emptyList())
    val loading by vm.loading.observeAsState(false)
    val context = LocalContext.current
    val activity = context as? Activity

    var showDeleteDialog by remember { mutableStateOf(false) }
    var selectedRouteId by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        vm.loadRoutes()
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
                            "Manage Routes",
                            style = TextStyle(color = Color.White, fontWeight = FontWeight.ExtraBold, fontSize = 20.sp)
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = { activity?.finish() }) {
                            Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                        }
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.Transparent)
                )
            },
            floatingActionButton = {
                FloatingActionButton(
                    onClick = {
                        val intent = Intent(context, AdminRouteSetupActivity::class.java)
                        context.startActivity(intent)
                    },
                    containerColor = PrimaryGreen,
                    contentColor = Color.White,
                    shape = CircleShape
                ) {
                    Icon(Icons.Default.Add, null)
                }
            }
        ) { padding ->
            // Parent Box for alignment
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {
                // Show Empty State only if not loading
                if (!loading && routes.isEmpty()) {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("No routes found. Create one!", color = Color.Gray)
                    }
                }

                // Main List
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(
                        start = 16.dp,
                        top = 8.dp,
                        end = 16.dp,
                        bottom = 100.dp // Added padding for FAB
                    )
                ) {
                    if (!loading) {
                        items(routes) { route ->
                            RouteAdminCard(
                                route = route,
                                onEdit = {
                                    val intent = Intent(context, AdminRouteSetupActivity::class.java)
                                    intent.putExtra("ROUTE_ID", route.routeId)
                                    context.startActivity(intent)
                                },
                                onDelete = {
                                    selectedRouteId = route.routeId
                                    showDeleteDialog = true
                                }
                            )
                        }
                    }
                }

                // Center Loader Overlay
                if (loading) {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center),
                        color = PrimaryGreen,
                        strokeWidth = 4.dp
                    )
                }
            }
        }
    }

    // Delete Dialog
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            shape = RoundedCornerShape(20.dp),
            title = { Text("Delete Route", fontWeight = FontWeight.Bold) },
            text = { Text("Are you sure you want to remove this route? This action cannot be undone.") },
            confirmButton = {
                TextButton(onClick = {
                    vm.deleteRoute(selectedRouteId) { success, msg ->
                        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                        if (success) {
                            vm.loadRoutes()
                            showDeleteDialog = false
                        }
                    }
                }) {
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

@Composable
fun RouteAdminCard(route: RouteModel, onEdit: () -> Unit, onDelete: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(50.dp)
                    .background(PrimaryGreen.copy(alpha = 0.1f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Route, null, tint = PrimaryGreen, modifier = Modifier.size(28.dp))
            }
            Spacer(Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(route.name, style = TextStyle(fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Color.Black))
                Text("Stops: ${route.points.size}", style = TextStyle(fontSize = 14.sp, color = Color.Gray))
            }
            Row {
                IconButton(onClick = onEdit) { Icon(Icons.Default.Edit, "Edit", tint = PrimaryGreen) }
                IconButton(onClick = onDelete) { Icon(Icons.Default.Delete, "Delete", tint = Color.Red) }
            }
        }
    }
}