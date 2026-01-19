package com.example.cleantrack.view.admin

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.cleantrack.R
import com.example.cleantrack.repository.UserRepoImpl
import com.example.cleantrack.repository.BinCollectionRepoImpl
import com.example.cleantrack.ui.theme.*
import com.example.cleantrack.view.common.LeaderboardActivity
import com.example.cleantrack.view.common.LogoutDialog
import com.example.cleantrack.view.common.PrivacyPolicyActivity
import com.example.cleantrack.view.common.TermsAndConditionActivity
import com.example.cleantrack.view.common.MarketplaceActivity
import com.example.cleantrack.viewmodel.UserViewModel

class AdminDashboardActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AdminDashboardBody()
        }
    }
}

@Composable
fun AdminDashboardBody() {
    val context = LocalContext.current
    val userViewModel = remember { UserViewModel(UserRepoImpl(), BinCollectionRepoImpl()) }
    val userProfile by userViewModel.user.observeAsState()
    var showLogoutDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        userViewModel.getCurrentUserId()?.let { uid ->
            userViewModel.getUserById(uid)
        }
    }

    LogoutDialog(
        showDialog = showLogoutDialog,
        onDismiss = { showLogoutDialog = false },
        viewModel = userViewModel
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(Green, Color.White),
                    startY = 0f,
                    endY = 1200f
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .padding(horizontal = 20.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(modifier = Modifier.height(20.dp))

            // --- HEADER SECTION ---
            Text(
                text = "Hello Admin ${userProfile?.fullname?.split(" ")?.firstOrNull() ?: ""} 👋",
                color = Color.White,
                fontSize = 26.sp,
                fontWeight = FontWeight.ExtraBold
            )
            Text(
                text = "System Control Center",
                color = Color.White.copy(alpha = 0.8f),
                fontSize = 16.sp
            )

            Spacer(modifier = Modifier.height(25.dp))

            // --- MANAGEMENT QUICK ACTIONS (Grid Style) ---
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text("Management Actions", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Black)
                    Spacer(modifier = Modifier.height(20.dp))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceAround) {
                        AdminQuickIcon(Icons.Default.People, "Users") {
                            context.startActivity(Intent(context, UserManagementActivity::class.java))
                        }
                        AdminQuickIcon(Icons.Default.Route, "Routes") {
                            context.startActivity(Intent(context, AdminRouteListActivity::class.java))
                        }
                        AdminQuickIcon(Icons.Default.CalendarMonth, "Schedules") {
                            context.startActivity(Intent(context, AdminScheduleListActivity::class.java))
                        }
                    }
                    Spacer(modifier = Modifier.height(20.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceAround) {
                        AdminQuickIcon(Icons.Default.DirectionsBus, "Vehicles") {
                            context.startActivity(Intent(context, AdminVehicleListActivity::class.java))
                        }
                        AdminQuickIcon(Icons.Default.Storefront, "Market") {
                            val intent = Intent(context, MarketplaceActivity::class.java).apply {
                                putExtra("USER_ID", userViewModel.getCurrentUserId())
                                putExtra("IS_ADMIN", true)
                            }
                            context.startActivity(intent)
                        }
                        AdminQuickIcon(Icons.Default.Leaderboard, "Leaderboard") {
                            context.startActivity(Intent(context, LeaderboardActivity::class.java))
                        }
                        // Placeholder to maintain grid alignment
//                        Box(modifier = Modifier.size(56.dp)) {}
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // --- FINANCE SETTINGS SECTION (List Style) ---
            Text(
                text = "Finance",
                color = Black,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(vertical = 10.dp)
            )

            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(4.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(8.dp)) {
                    AdminMenuItem(Icons.Default.Payments, "View Transactions") {
                        context.startActivity(Intent(context, AdminTransactionListActivity::class.java))
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // --- SYSTEM SETTINGS SECTION (List Style) ---
            Text(
                text = "System Settings",
                color = Black,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(vertical = 10.dp)
            )

            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(4.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(8.dp)) {
                    AdminMenuItem(Icons.Default.Policy, "Privacy Policy") {
                        context.startActivity(Intent(context, PrivacyPolicyActivity::class.java))
                    }
                    HorizontalDivider(thickness = 0.5.dp, color = Color.LightGray, modifier = Modifier.padding(horizontal = 16.dp))

                    AdminMenuItem(Icons.Default.Gavel, "Terms & Conditions") {
                        context.startActivity(Intent(context, TermsAndConditionActivity::class.java))
                    }
                    HorizontalDivider(thickness = 0.5.dp, color = Color.LightGray, modifier = Modifier.padding(horizontal = 16.dp))

                    AdminMenuItem(Icons.Default.Rule, "Points Rules") {
                        context.startActivity(Intent(context, AdminPointsRuleListActivity::class.java))
                    }
                    HorizontalDivider(thickness = 0.5.dp, color = Color.LightGray, modifier = Modifier.padding(horizontal = 16.dp))

                    AdminMenuItem(Icons.Default.Announcement, "Announcements") {
                        context.startActivity(Intent(context, AdminAnnouncementListActivity::class.java))
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // --- LOGOUT BUTTON ---
            Button(
                onClick = { showLogoutDialog = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Red.copy(alpha = 0.1f)),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, Red)
            ) {
                Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = null, tint = Red)
                Spacer(modifier = Modifier.width(10.dp))
                Text("Sign Out", color = Red, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }

            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}

@Composable
fun AdminQuickIcon(icon: ImageVector, label: String, onClick: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable { onClick() }
    ) {
        Box(
            modifier = Modifier
                .size(56.dp)
                .background(Green.copy(alpha = 0.1f), CircleShape)
                .border(1.dp, Green, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, null, tint = Green, modifier = Modifier.size(26.dp))
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(label, fontSize = 12.sp, color = Black, fontWeight = FontWeight.Medium)
    }
}

@Composable
fun AdminMenuItem(icon: ImageVector, label: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(18.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, null, tint = Green, modifier = Modifier.size(24.dp))
        Spacer(modifier = Modifier.width(15.dp))
        Text(label, color = Black, fontSize = 16.sp, fontWeight = FontWeight.Medium)
        Spacer(modifier = Modifier.weight(1f))
        Icon(Icons.Default.ChevronRight, null, tint = Color.LightGray)
    }
}