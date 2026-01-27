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
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.cleantrack.R
import com.example.cleantrack.model.RouteInsightModel
import com.example.cleantrack.model.UserModel
import com.example.cleantrack.repository.AIRepository
import com.example.cleantrack.repository.UserRepoImpl
import com.example.cleantrack.repository.BinCollectionRepoImpl
import com.example.cleantrack.repository.NotificationRepoImpl
import com.example.cleantrack.ui.theme.*
import com.example.cleantrack.view.common.*
import com.example.cleantrack.viewmodel.NotificationViewModel
import com.example.cleantrack.viewmodel.UserViewModel
import com.example.cleantrack.util.NotificationHelper
import kotlinx.coroutines.launch

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
    val notificationViewModel = remember { NotificationViewModel(NotificationRepoImpl(), UserRepoImpl()) }


    val userProfile by userViewModel.user.observeAsState()
    val notifications by notificationViewModel.notifications.observeAsState(emptyList())

    val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current
    val lifecycleState by lifecycleOwner.lifecycle.currentStateFlow.collectAsState()

    var selectedTab by remember { mutableIntStateOf(0) }
    var showLogoutDialog by remember { mutableStateOf(false) }
    var shownNotificationIds by remember { mutableStateOf(setOf<String>()) }

    val aiRepo = remember { AIRepository() }
    val scope = rememberCoroutineScope()
    var aiStrategy by remember { mutableStateOf("Generating operational strategy...") }

    // Initial Data Fetch
    LaunchedEffect(Unit) {
        userViewModel.getCurrentUserId()?.let { uid ->
            userViewModel.getUserById(uid)
            notificationViewModel.observeNotifications(uid)
        }





        // Fetch RouteInsights
        val insightRef = com.google.firebase.database.FirebaseDatabase.getInstance().getReference("RouteInsights")
        insightRef.get().addOnSuccessListener { snapshot ->
            val insights = snapshot.children.mapNotNull { it.getValue(RouteInsightModel::class.java) }
            if (insights.isNotEmpty()) {
                scope.launch { aiStrategy = aiRepo.generateStrategyFromInsights(insights) }
            } else {
                aiStrategy = "No historical data found for AI report."
            }
        }
    }

    LaunchedEffect(lifecycleState) {
        val uid = userViewModel.getCurrentUserId()
        if (uid != null && lifecycleState == androidx.lifecycle.Lifecycle.State.RESUMED) {
            userViewModel.refreshUser(uid) // This fetch will now pick up the new timestamped URL
        }
    }

    LogoutDialog(showDialog = showLogoutDialog, onDismiss = { showLogoutDialog = false }, viewModel = userViewModel)

    Box(modifier = Modifier.fillMaxSize().background(brush = Brush.verticalGradient(colors = listOf(Green, Color.White), startY = 0f, endY = 1400f))) {
        Scaffold(
            containerColor = Color.Transparent,
            bottomBar = {
                Surface(modifier = Modifier.fillMaxWidth(), color = Color.White, tonalElevation = 8.dp, shadowElevation = 15.dp) {
                    Row(modifier = Modifier.navigationBarsPadding().padding(vertical = 12.dp).fillMaxWidth(), horizontalArrangement = Arrangement.SpaceAround) {
                        AdminBottomNavItem(Icons.Outlined.Dashboard, "Home", active = selectedTab == 0) { selectedTab = 0 }
                        AdminBottomNavItem(Icons.Outlined.Payments, "Finance", active = selectedTab == 1) { selectedTab = 1 }
                        AdminBottomNavItem(Icons.Outlined.ConfirmationNumber, "Tickets", active = selectedTab == 2) { selectedTab = 2 }
                        AdminBottomNavItem(Icons.Outlined.AdminPanelSettings, "Profile", active = selectedTab == 3) { selectedTab = 3 }
                    }
                }
            }
        ) { innerPadding ->
            when (selectedTab) {
                0 -> AdminHomeSection(innerPadding, userProfile, aiStrategy, userViewModel)
                1 -> AdminFinanceSection(innerPadding)
                2 -> AdminTicketSection(innerPadding)
                3 -> AdminProfileSection(innerPadding, userProfile) { showLogoutDialog = true }
            }
        }
    }
}

@Composable
fun AdminHomeSection(padding: PaddingValues, userProfile: UserModel?, aiStrategy: String, userViewModel: UserViewModel) {
    val context = LocalContext.current

    Column(modifier = Modifier.fillMaxSize().padding(padding).padding(horizontal = 20.dp).verticalScroll(rememberScrollState())) {
        Spacer(modifier = Modifier.height(20.dp))

        // Enhanced Header matching User Dashboard
        Text(text = "Hello Admin ${userProfile?.fullname?.split(" ")?.firstOrNull() ?: ""} 👋", color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.ExtraBold)
        Spacer(modifier = Modifier.height(15.dp))

        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Surface(
                shape = CircleShape,
                color = Color.White.copy(0.4f),
                modifier = Modifier.size(45.dp).clickable {
                    val intent = Intent(context, EditProfileActivity::class.java).apply { putExtra("userId", userProfile?.userId) }
                    context.startActivity(intent)
                },
                border = BorderStroke(1.dp, Color.White.copy(0.5f))
            ) {
                AsyncImage(
                    model = if (!userProfile?.profileImageUrl.isNullOrEmpty()) userProfile?.profileImageUrl else R.drawable.user_logo,
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize().clip(CircleShape),
                    contentScale = ContentScale.Crop
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Text(text = "System Online", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.weight(1f))
            IconButton(onClick = { context.startActivity(Intent(context, NotificationCenterActivity::class.java)) }) {
                Icon(Icons.Outlined.Notifications, null, tint = Color.White, modifier = Modifier.size(26.dp))
            }
        }

        Spacer(modifier = Modifier.height(25.dp))

        // AI Strategy Card
        Text("Operational Intelligence", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = White),
            elevation = CardDefaults.cardElevation(6.dp),
            modifier = Modifier.fillMaxWidth().padding(vertical = 10.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.AutoAwesome, "AI", tint = Green)
                    Spacer(modifier = Modifier.width(10.dp))
                    Text("AI Route Strategy", fontWeight = FontWeight.ExtraBold, color = Green)
                }
                Spacer(modifier = Modifier.height(10.dp))
                Text(text = aiStrategy, fontSize = 14.sp, color = Color.DarkGray, lineHeight = 20.sp)
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Management Actions Card
        Card(shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(containerColor = White), elevation = CardDefaults.cardElevation(6.dp), modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text("Core Management", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Spacer(modifier = Modifier.height(20.dp))
                Row(modifier = Modifier.fillMaxWidth(), Arrangement.SpaceAround) {
                    AdminQuickIcon(Icons.Default.People, "Users") { context.startActivity(Intent(context, UserManagementActivity::class.java)) }
                    AdminQuickIcon(Icons.Default.Route, "Routes") { context.startActivity(Intent(context, AdminRouteListActivity::class.java)) }
                    AdminQuickIcon(Icons.Default.CalendarMonth, "Schedules") { context.startActivity(Intent(context, AdminScheduleListActivity::class.java)) }
                }
                Spacer(modifier = Modifier.height(25.dp))
                Row(modifier = Modifier.fillMaxWidth(), Arrangement.SpaceAround) {
                    AdminQuickIcon(Icons.Default.DirectionsBus, "Vehicles") { context.startActivity(Intent(context, AdminVehicleListActivity::class.java)) }
                    AdminQuickIcon(Icons.Default.Storefront, "Market") {
                        context.startActivity(Intent(context, MarketplaceActivity::class.java).apply {
                            putExtra("USER_ID", userViewModel.getCurrentUserId())
                            putExtra("IS_ADMIN", true)
                        })
                    }
                    AdminQuickIcon(Icons.Default.Leaderboard, "Leaderboard") { context.startActivity(Intent(context, LeaderboardActivity::class.java)) }
                }
            }
        }
        Spacer(modifier = Modifier.height(30.dp))
    }
}

@Composable
fun AdminFinanceSection(padding: PaddingValues) {
    val context = LocalContext.current
    Column(modifier = Modifier.fillMaxSize().padding(padding).padding(20.dp)) {
        Text("Finance Management", color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(20.dp))
        Card(shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(containerColor = White), modifier = Modifier.fillMaxWidth()) {
            AdminMenuItem(Icons.Default.Payments, "View All Transactions") {
                context.startActivity(Intent(context, AdminTransactionListActivity::class.java))
            }
        }
    }
}

@Composable
fun AdminTicketSection(padding: PaddingValues) {
    val context = LocalContext.current
    Column(modifier = Modifier.fillMaxSize().padding(padding).padding(20.dp)) {
        Text("Support Tickets", color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(20.dp))
        Card(shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(containerColor = White), modifier = Modifier.fillMaxWidth()) {
            AdminMenuItem(Icons.Default.ContactSupport, "Pending Requests") {
                context.startActivity(Intent(context, AdminContactSupportViewActivity::class.java))
            }
        }
    }
}

@Composable
fun AdminProfileSection(padding: PaddingValues, userProfile: UserModel?, onLogout: () -> Unit) {
    val context = LocalContext.current
    Column(modifier = Modifier.fillMaxSize().padding(padding).padding(20.dp).verticalScroll(rememberScrollState()), horizontalAlignment = Alignment.CenterHorizontally) {
        Spacer(modifier = Modifier.height(30.dp))
        Surface(shape = CircleShape, color = White.copy(0.3f), modifier = Modifier.size(110.dp).clickable {
            val intent = Intent(context, EditProfileActivity::class.java).apply { putExtra("userId", userProfile?.userId) }
            context.startActivity(intent)
        }, border = BorderStroke(2.dp, White)) {
            AsyncImage(model = if (!userProfile?.profileImageUrl.isNullOrEmpty()) userProfile?.profileImageUrl else R.drawable.user_logo, contentDescription = null, modifier = Modifier.fillMaxSize().clip(CircleShape), contentScale = ContentScale.Crop)
        }
        Spacer(modifier = Modifier.height(15.dp))
        Text(userProfile?.fullname ?: "Admin", color = White, fontSize = 24.sp, fontWeight = FontWeight.ExtraBold)
        Text("Administrator Account", color = White.copy(0.8f), fontSize = 14.sp)

        Spacer(modifier = Modifier.height(40.dp))

        Card(shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(containerColor = White), modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(8.dp)) {
                AdminMenuItem(Icons.Default.Person, "Edit Profile") {
                    val intent = Intent(context, EditProfileActivity::class.java).apply { putExtra("userId", userProfile?.userId) }
                    context.startActivity(intent)
                }
                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), thickness = 0.5.dp, color = Color.LightGray)

                // Shifted System Settings here
                AdminMenuItem(Icons.Default.Announcement, "Manage Announcements") {
                    context.startActivity(Intent(context, AdminAnnouncementListActivity::class.java))
                }
                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), thickness = 0.5.dp, color = Color.LightGray)

                AdminMenuItem(Icons.Default.Rule, "Points System Rules") {
                    context.startActivity(Intent(context, AdminPointsRuleListActivity::class.java))
                }
                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), thickness = 0.5.dp, color = Color.LightGray)

                AdminMenuItem(Icons.Default.Policy, "Privacy Policy") {
                    context.startActivity(Intent(context, PrivacyPolicyActivity::class.java))
                }
                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), thickness = 0.5.dp, color = Color.LightGray)

                AdminMenuItem(Icons.Default.Gavel, "Terms & Conditions") {
                    context.startActivity(Intent(context, TermsAndConditionActivity::class.java))
                }
                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), thickness = 0.5.dp, color = Color.LightGray)

                AdminMenuItem(Icons.AutoMirrored.Filled.Logout, "Sign Out", textColor = Red) { onLogout() }
            }
        }
    }
}

@Composable
fun AdminBottomNavItem(icon: ImageVector, label: String, active: Boolean, onClick: () -> Unit) {
    val color = if (active) Green else Color.Gray
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.clickable { onClick() }.padding(horizontal = 12.dp)) {
        Icon(icon, null, tint = color, modifier = Modifier.size(26.dp))
        Text(text = label, color = color, fontSize = 12.sp, fontWeight = if (active) FontWeight.Bold else FontWeight.Normal)
    }
}

// Keep your existing AdminMenuItem and AdminQuickIcon, added textColor parameter to MenuItem
@Composable
fun AdminMenuItem(icon: ImageVector, label: String, textColor: Color = Black, onClick: () -> Unit) {
    Row(modifier = Modifier.fillMaxWidth().clickable { onClick() }.padding(18.dp), verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, null, tint = if (textColor == Red) Red else Green, modifier = Modifier.size(24.dp))
        Spacer(modifier = Modifier.width(15.dp))
        Text(label, color = textColor, fontSize = 16.sp, fontWeight = FontWeight.Medium)
        Spacer(modifier = Modifier.weight(1f))
        Icon(Icons.Default.ChevronRight, null, tint = Color.LightGray)
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
                .background(Green, CircleShape), // Solid Green background like UserDashboard
            contentAlignment = Alignment.Center
        ) {
            Icon(
                icon,
                contentDescription = label,
                tint = Color.White, // White icon for better contrast
                modifier = Modifier.size(26.dp)
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = label,
            fontSize = 12.sp,
            color = Black,
            fontWeight = FontWeight.Medium
        )
    }
}