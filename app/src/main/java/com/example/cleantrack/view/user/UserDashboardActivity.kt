package com.example.cleantrack.view.user

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
import com.example.cleantrack.model.AnnouncementModel
import com.example.cleantrack.model.BinCollectionModel
import com.example.cleantrack.model.UserModel
import com.example.cleantrack.repository.AIRepository
import com.example.cleantrack.repository.AnnouncementRepoImpl
import com.example.cleantrack.repository.BinCollectionRepoImpl
import com.example.cleantrack.repository.UserRepoImpl
import com.example.cleantrack.ui.theme.*
import com.example.cleantrack.view.common.AnnouncementBanner
import com.example.cleantrack.view.common.LogoutDialog
import com.example.cleantrack.view.common.MarketplaceActivity
import com.example.cleantrack.view.common.PrivacyPolicyActivity
import com.example.cleantrack.viewmodel.AnnouncementViewModel
import com.example.cleantrack.viewmodel.UserViewModel

class UserDashboardActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        org.maplibre.android.MapLibre.getInstance(this)
        enableEdgeToEdge()
        setContent {
            UserDashboardBody()
        }
    }
}

@Composable
fun UserDashboardBody() {
    val context = LocalContext.current
    val userViewModel = remember { UserViewModel(UserRepoImpl(), BinCollectionRepoImpl()) }

    val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current
    val lifecycleState by lifecycleOwner.lifecycle.currentStateFlow.collectAsState()

    val userProfile by userViewModel.user.observeAsState()
    val currentPoints by userViewModel.userPoints.observeAsState(0)
    val latestCollection by userViewModel.latestCollection.observeAsState()
    val globalAiReview by userViewModel.globalAiReview.observeAsState("Analyzing your waste habits...")

    // NEW: Observe loading state from ViewModel
    val isLoadingUser by userViewModel.loading.observeAsState(true)

    var selectedTab by remember { mutableIntStateOf(0) }
    var showLogoutDialog by remember { mutableStateOf(false) }
    var showPremiumDialog by remember { mutableStateOf(false) }

    // Logic to check premium status
    val isPremium = remember(userProfile) { userViewModel.isPremiumUser(userProfile) }

    val aiRepo = remember { AIRepository() }

    LaunchedEffect(Unit) {
        userViewModel.getCurrentUserId()?.let { uid ->
            userViewModel.getUserById(uid)
            userViewModel.fetchUserPoints(uid)
            userViewModel.fetchGlobalAIReview(uid, aiRepo)
        }
    }

    LaunchedEffect(lifecycleState) {
        val uid = userViewModel.getCurrentUserId()
        if (uid != null && lifecycleState == androidx.lifecycle.Lifecycle.State.RESUMED) {
            userViewModel.refreshUser(uid)
            userViewModel.fetchUserPoints(uid)
        }
    }

    if (showPremiumDialog) {
        AlertDialog(
            onDismissRequest = { showPremiumDialog = false },
            title = { Text("Premium Feature 🌟", fontWeight = FontWeight.Bold) },
            text = { Text("Routes, Schedules, and Bin Management are premium features. Upgrade now to optimize your waste collection!") },
            confirmButton = {
                Button(
                    onClick = {
                        showPremiumDialog = false
                        context.startActivity(Intent(context, PaymentActivity::class.java).apply {
                            putExtra("USER_ID", userProfile?.userId) // PASSING ID HERE
                        })
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Green)
                ) { Text("Upgrade Now", color = Color.White) }
            },
            dismissButton = {
                TextButton(onClick = { showPremiumDialog = false }) { Text("Maybe Later") }
            },
            shape = RoundedCornerShape(20.dp)
        )
    }

    LogoutDialog(showDialog = showLogoutDialog, onDismiss = { showLogoutDialog = false }, viewModel = userViewModel)

    Box(modifier = Modifier.fillMaxSize().background(brush = Brush.verticalGradient(colors = listOf(Blue, Green, Color.White), startY = 0f, endY = 1400f))) {
        Scaffold(
            containerColor = Color.Transparent,
            bottomBar = {
                Surface(modifier = Modifier.fillMaxWidth(), color = Color.White, tonalElevation = 8.dp, shadowElevation = 15.dp) {
                    Row(modifier = Modifier.navigationBarsPadding().padding(vertical = 12.dp).fillMaxWidth(), horizontalArrangement = Arrangement.SpaceAround) {
                        BottomNavItem(Icons.Outlined.Home, "Home", active = selectedTab == 0) { selectedTab = 0 }
                        // PREMIUM GATE: Bottom Navigation Tracker
                        BottomNavItem(Icons.Outlined.Explore, "Tracker", active = selectedTab == 1) {
                            if (isPremium) {
                                selectedTab = 1
                            } else {
                                showPremiumDialog = true
                            }
                        }
                        BottomNavItem(Icons.Outlined.PersonOutline, "Profile", active = selectedTab == 2) { selectedTab = 2 }
                    }
                }
            }
        ) { innerPadding ->
            when (selectedTab) {
                // Pass isLoadingUser to HomeSection
                0 -> HomeSection(innerPadding, userViewModel, userProfile, currentPoints, latestCollection, globalAiReview, isLoadingUser) { showPremiumDialog = true }
                1 -> MapTrackerSection(innerPadding, userProfile)
                2 -> ProfileSection(innerPadding, userProfile) { showLogoutDialog = true }
            }
        }
    }
}

@Composable
fun HomeSection(
    padding: PaddingValues,
    userViewModel: UserViewModel,
    userProfile: UserModel?,
    currentPoints: Int,
    latestCollection: BinCollectionModel?,
    globalAiReview: String,
    isLoading: Boolean, // Added parameter
    onShowPremiumGate: () -> Unit
) {
    val context = LocalContext.current
    val currentUserId = userViewModel.getCurrentUserId() ?: ""
    val announcementVM = remember { AnnouncementViewModel(AnnouncementRepoImpl()) }
    val announcements by announcementVM.allAnnouncements.observeAsState(emptyList())

    // Determine premium status
    val isPremium = remember(userProfile) { userViewModel.isPremiumUser(userProfile) }

    var showAnnouncement by remember { mutableStateOf(false) }
    var latestUnseenAnnouncement by remember { mutableStateOf<AnnouncementModel?>(null) }

    LaunchedEffect(Unit) { announcementVM.getAllAnnouncements { _, _, _ -> } }
    LaunchedEffect(announcements) {
        val unseen = announcements?.firstOrNull { it.seenBy[currentUserId] != true }
        if (unseen != null) {
            latestUnseenAnnouncement = unseen
            showAnnouncement = true
        }
    }

    Column(modifier = Modifier.fillMaxSize().padding(padding).padding(horizontal = 20.dp).verticalScroll(rememberScrollState())) {
        if (showAnnouncement && latestUnseenAnnouncement != null) {
            AnnouncementBanner(announcement = latestUnseenAnnouncement!!, onDismiss = { showAnnouncement = false; announcementVM.markAsSeen(latestUnseenAnnouncement!!.id, currentUserId) })
        }
        Spacer(modifier = Modifier.height(20.dp))
        Text(text = "Hello ${userProfile?.fullname?.split(" ")?.firstOrNull() ?: "User"} ☀️", color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.ExtraBold)
        Spacer(modifier = Modifier.height(15.dp))

        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Surface(shape = CircleShape, color = Color.White.copy(0.4f), modifier = Modifier.size(45.dp), border = BorderStroke(1.dp, Color.White.copy(0.5f))) {
                AsyncImage(model = if (!userProfile?.profileImageUrl.isNullOrEmpty()) userProfile?.profileImageUrl else R.drawable.user_logo, contentDescription = null, modifier = Modifier.fillMaxSize().clip(CircleShape), contentScale = ContentScale.Crop)
            }
            Spacer(modifier = Modifier.width(12.dp))
            Text(text = "Points: $currentPoints ✨", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.weight(1f))
            IconButton(onClick = { context.startActivity(Intent(context, UserAnnouncementListActivity::class.java)) }) { Icon(Icons.Outlined.Campaign, null, tint = Color.White, modifier = Modifier.size(28.dp)) }
        }

        Spacer(modifier = Modifier.height(25.dp))

        // PREMIUM GATE: Live Tracker Card
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = White),
            elevation = CardDefaults.cardElevation(6.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .clickable {
                    if (isPremium) {
                        userProfile?.activeRouteId?.let { id ->
                            context.startActivity(Intent(context, TruckLiveTrackingActivity::class.java).apply {
                                putExtra("ROUTE_ID", id)
                            })
                        }
                    } else {
                        onShowPremiumGate()
                    }
                }
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Live Garbage Truck Tracker", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Black)
                    if (!isPremium) {
                        Spacer(Modifier.width(8.dp))
                        Icon(Icons.Default.Lock, contentDescription = null, tint = Green, modifier = Modifier.size(16.dp))
                    }
                }
                Spacer(modifier = Modifier.height(10.dp))
                Box(modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(14.dp)), contentAlignment = Alignment.Center) {
                    if (isLoading) {
                        CircularProgressIndicator(color = Green, modifier = Modifier.size(30.dp))
                    } else if (isPremium && !userProfile?.activeRouteId.isNullOrEmpty()) {
                        TruckLiveMapScreen(routeId = userProfile!!.activeRouteId)
                    } else {
                        // Blurred or placeholder view for non-premium
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.Map, contentDescription = null, tint = Color.LightGray, modifier = Modifier.size(40.dp))
                            Text(
                                text = if (isPremium) "No active route selected" else "Upgrade to see live location",
                                fontSize = 13.sp,
                                color = Color.Gray
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Operations Card with Loading Prevention
        Card(shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(containerColor = White), elevation = CardDefaults.cardElevation(6.dp), modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text("Operations", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Spacer(modifier = Modifier.height(20.dp))

                if (isLoading) {
                    // Show a clean placeholder while checking subscription to avoid flickering icons
                    Box(Modifier.fillMaxWidth().height(140.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = Green, strokeWidth = 2.dp)
                    }
                } else {
                    Row(modifier = Modifier.fillMaxWidth(), Arrangement.SpaceAround) {
                        QuickIcon(Icons.Outlined.PhotoCamera, "Scan", isOutline = true)
                        QuickIcon(Icons.Outlined.SwapVert, "Exchange", isOutline = true, isSpecial = true)
                        QuickIcon(Icons.Outlined.Terrain, "Landfill", isOutline = true)
                        LockedQuickIcon(icon = Icons.Default.RestoreFromTrash, label = "Manage Bins", isLocked = !isPremium) {
                            if (isPremium) context.startActivity(Intent(context, UserBinListActivity::class.java)) else onShowPremiumGate()
                        }
                    }
                    Spacer(modifier = Modifier.height(25.dp))
                    Row(modifier = Modifier.fillMaxWidth(), Arrangement.SpaceAround) {
                        QuickIcon(Icons.Default.CreditCard, "Payments") { context.startActivity(Intent(context, PaymentActivity::class.java).apply {
                            putExtra("USER_ID", userProfile?.userId) // PASSING ID HERE
                        }) }
                        QuickIcon(icon = Icons.Default.Route, label = "Routes") {
                             context.startActivity(Intent(context, UserRouteLiveTrackingActivity::class.java))
                        }
                        LockedQuickIcon(icon = Icons.Default.CalendarMonth, label = "Schedule", isLocked = !isPremium) {
                            if (isPremium) context.startActivity(Intent(context, UserScheduleListActivity::class.java)) else onShowPremiumGate()
                        }
                        QuickIcon(Icons.Default.ShoppingBag, "Market") { context.startActivity(Intent(context, MarketplaceActivity::class.java).apply { putExtra("USER_ID", currentUserId) }) }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // AI Card
        Card(shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(containerColor = White), elevation = CardDefaults.cardElevation(6.dp), modifier = Modifier.fillMaxWidth().wrapContentHeight()) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(text = "AI Smart Assist", fontSize = 20.sp, fontWeight = FontWeight.ExtraBold, color = Green)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(text = globalAiReview, fontSize = 14.sp, color = Color.DarkGray, lineHeight = 20.sp)
                    }
                    Icon(Icons.Default.AutoAwesome, null, tint = Green.copy(0.5f), modifier = Modifier.size(35.dp))
                }
            }
        }
        Spacer(modifier = Modifier.height(30.dp))
    }
}

// ... Rest of your composables (LockedQuickIcon, MapTrackerSection, etc.) remain the same ...

@Composable
fun LockedQuickIcon(icon: ImageVector, label: String, isLocked: Boolean, onClick: () -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.clickable { onClick() }) {
        Box(contentAlignment = Alignment.Center) {
            Box(modifier = Modifier.size(56.dp).background(if (isLocked) Color(0xFFE0E0E0) else Green, CircleShape), contentAlignment = Alignment.Center) {
                Icon(icon, null, tint = if (isLocked) Color.Gray else Color.White, modifier = Modifier.size(26.dp))
            }
            if (isLocked) {
                Surface(shape = CircleShape, color = Green, modifier = Modifier.size(20.dp).align(Alignment.TopEnd), border = BorderStroke(1.dp, Color.White)) {
                    Icon(Icons.Default.Lock, null, tint = Color.White, modifier = Modifier.padding(3.dp))
                }
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(label, fontSize = 12.sp, color = if (isLocked) Color.Gray else Black, fontWeight = FontWeight.Medium)
    }
}

@Composable
fun MapTrackerSection(padding: PaddingValues, userProfile: UserModel?) {
    Column(modifier = Modifier.fillMaxSize().padding(padding).padding(20.dp)) {
        Text("Vehicle Tracking", color = Color.White, fontSize = 26.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(20.dp))
        Card(modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(28.dp)), elevation = CardDefaults.cardElevation(8.dp), colors = CardDefaults.cardColors(containerColor = White)) {
            if (!userProfile?.activeRouteId.isNullOrEmpty()) { UserLiveMapScreen(routeId = userProfile!!.activeRouteId) }
            else { Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text("Select a route in Home to start tracking", color = Color.Gray) } }
        }
    }
}

@Composable
fun ProfileSection(padding: PaddingValues, userProfile: UserModel?, onLogout: () -> Unit) {
    val context = LocalContext.current
    Column(modifier = Modifier.fillMaxSize().padding(padding).padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Spacer(modifier = Modifier.height(30.dp))
        Surface(shape = CircleShape, color = White.copy(0.3f), modifier = Modifier.size(110.dp), border = BorderStroke(2.dp, White)) {
            AsyncImage(model = if (!userProfile?.profileImageUrl.isNullOrEmpty()) userProfile?.profileImageUrl else R.drawable.user_logo, contentDescription = null, modifier = Modifier.fillMaxSize().clip(CircleShape), contentScale = ContentScale.Crop)
        }
        Spacer(modifier = Modifier.height(15.dp))
        Text(userProfile?.fullname ?: "User Name", color = White, fontSize = 24.sp, fontWeight = FontWeight.ExtraBold)
        Text(userProfile?.email ?: "", color = White.copy(0.8f), fontSize = 15.sp)
        Spacer(modifier = Modifier.height(40.dp))
        Card(shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(containerColor = White), modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(8.dp)) {
                ProfileMenuItem(Icons.Default.Policy, "Privacy Policy") { context.startActivity(Intent(context, PrivacyPolicyActivity::class.java)) }
                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), thickness = 0.5.dp, color = Color.LightGray)
                ProfileMenuItem(Icons.AutoMirrored.Filled.Logout, "Logout", textColor = Red) { onLogout() }
            }
        }
    }
}

@Composable
fun ProfileMenuItem(icon: ImageVector, label: String, textColor: Color = Black, onClick: () -> Unit) {
    Row(modifier = Modifier.fillMaxWidth().clickable { onClick() }.padding(18.dp), verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, null, tint = if (textColor == Red) Red else Green, modifier = Modifier.size(24.dp))
        Spacer(modifier = Modifier.width(15.dp))
        Text(label, color = textColor, fontSize = 16.sp, fontWeight = FontWeight.Medium)
        Spacer(modifier = Modifier.weight(1f))
        Icon(Icons.Default.ChevronRight, null, tint = Color.LightGray)
    }
}

@Composable
fun BottomNavItem(icon: ImageVector, label: String, active: Boolean, onClick: () -> Unit) {
    val color = if (active) Green else Color.Gray
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.clickable { onClick() }.padding(horizontal = 12.dp)) {
        Icon(icon, null, tint = color, modifier = Modifier.size(26.dp))
        Text(text = label, color = color, fontSize = 12.sp, fontWeight = if (active) FontWeight.Bold else FontWeight.Normal)
    }
}

@Composable
fun QuickIcon(icon: ImageVector, label: String = "", isOutline: Boolean = false, isSpecial: Boolean = false, onClick: () -> Unit = {}) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.clickable { onClick() }) {
        Box(modifier = Modifier.size(56.dp).background(if (isOutline) Color.Transparent else Green, CircleShape).then(if (isOutline) Modifier.border(2.dp, if (isSpecial) Blue else Green, CircleShape) else Modifier), contentAlignment = Alignment.Center) {
            Icon(icon, null, tint = if (isOutline) Green else Color.White, modifier = Modifier.size(26.dp))
        }
        if (label.isNotEmpty()) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(label, fontSize = 12.sp, color = Black, fontWeight = FontWeight.Medium)
        }
    }
}