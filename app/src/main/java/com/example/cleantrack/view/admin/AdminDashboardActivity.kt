package com.example.cleantrack.view.admin

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.cleantrack.view.common.PrivacyPolicyActivity
import com.example.cleantrack.view.auth.StartActivity

class AdminDashboardActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            AdminDashboardScreen()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminDashboardScreen() {

    val context = LocalContext.current
    val activity = context as Activity

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Admin Dashboard") },
                actions = {
                    IconButton(onClick = {
                        // Simple logout navigation
                        activity.startActivity(
                            Intent(context, StartActivity::class.java)
                        )
                        activity.finish()
                    }) {
                        Icon(Icons.Default.Logout, contentDescription = "Logout")
                    }
                }
            )
        }
    ) { padding ->

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            item {
                Text(
                    "Management",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            item {
                DashboardCard(
                    title = "Manage Users",
                    icon = Icons.Default.People
                ) {
                    // TODO: Replace with UserManagementActivity
                     context.startActivity(Intent(context, UserManagementActivity::class.java))
                }
            }

            item {
                DashboardCard(
                    title = "Manage Routes",
                    icon = Icons.Default.Map
                ) {
                    context.startActivity(
                        Intent(context, AdminRouteListActivity::class.java)
                    )
                }
            }

            item {
                DashboardCard(
                    title = "Manage Schedules",
                    icon = Icons.Default.Schedule
                ) {
                    context.startActivity(
                        Intent(context, AdminScheduleListActivity::class.java))
                }
            }

            item {
                DashboardCard(
                    title = "Manage Vehicles",
                    icon = Icons.Default.DirectionsBus
                ) {
                    // TODO: Replace with VehicleManagementActivity
                    context.startActivity(
                        Intent(context, AdminVehicleListActivity::class.java)
                    )
                }
            }

            item {
                Text(
                    "System",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            item {
                DashboardCard(
                    title = "Privacy Policy",
                    icon = Icons.Default.Policy
                ) {
                    context.startActivity(
                        Intent(context, PrivacyPolicyActivity::class.java)
                    )
                }
            }
        }
    }
}

@Composable
fun DashboardCard(
    title: String,
    icon: ImageVector,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFF5F5F5)
        ),
        elevation = CardDefaults.cardElevation(6.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                icon,
                contentDescription = null,
                modifier = Modifier.size(32.dp)
            )

            Spacer(modifier = Modifier.width(16.dp))

            Text(
                text = title,
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}
