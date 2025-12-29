package com.example.cleantrack.view.user

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.Icon
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.filled.Announcement
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Policy
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.cleantrack.repository.UserRepoImpl
import com.example.cleantrack.R
import com.example.cleantrack.ui.theme.Black
import com.example.cleantrack.ui.theme.Red
import com.example.cleantrack.ui.theme.White
import com.example.cleantrack.view.admin.DashboardCard
import com.example.cleantrack.view.auth.StartActivity
import com.example.cleantrack.view.common.LogoutDialog
import com.example.cleantrack.view.common.PrivacyPolicyActivity
import com.example.cleantrack.viewmodel.UserViewModel
import androidx.compose.runtime.livedata.observeAsState
import com.example.cleantrack.model.AnnouncementModel
import com.example.cleantrack.repository.AnnouncementRepoImpl
import com.example.cleantrack.view.common.AnnouncementBanner // Import the banner we designed earlier
import com.example.cleantrack.viewmodel.AnnouncementViewModel

class UserDashboardActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            UserDashboardBody()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserDashboardBody() {

    val context = LocalContext.current
    val activity = context as Activity

    val userViewModel = remember { UserViewModel(UserRepoImpl()) }

    // 1. Initialize Announcement ViewModel
    val announcementVM = remember { AnnouncementViewModel(AnnouncementRepoImpl()) }

    // 2. State for the Popup/Banner
    val announcements by announcementVM.allAnnouncements.observeAsState(emptyList())
    var showAnnouncement by remember { mutableStateOf(false) }
    var latestAnnouncement by remember { mutableStateOf<AnnouncementModel?>(null) }

    var showLogoutDialog by remember { mutableStateOf(false) }

    // 2. Trigger the fetch once
    LaunchedEffect(Unit) {
        announcementVM.getAllAnnouncements { _, _, _ -> }
    }

// 3. React to DATA changes (This is the "Pop-up" trigger)
    LaunchedEffect(announcements) {
        // Add a log here to see if data is actually arriving
        android.util.Log.d("ANNOUNCEMENT_DEBUG", "List Size: ${announcements?.size}")

        if (!announcements.isNullOrEmpty()) {
            latestAnnouncement = announcements!!.first()
            showAnnouncement = true
        }
    }

    LogoutDialog(
        showDialog = showLogoutDialog,
        onDismiss = { showLogoutDialog = false },
        viewModel = userViewModel
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "User Dashboard",
                            style = TextStyle(
                                fontSize = 22.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Black,
                                textAlign = TextAlign.Center
                            )
                        )
                        Text(
                            text = "Welcome 👋",
                            fontSize = 14.sp,
                            color = Black.copy(alpha = 0.7f),
                            textAlign = TextAlign.Center
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            context.startActivity(
                                Intent(context, SettingsActivity::class.java)
                            )
                        }
                    ) {
                        Icon(
                            Icons.Default.Settings, null
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = White
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = {
                showLogoutDialog = true
            }) {
                Icon(
                    Icons.Default.Logout,
                    contentDescription = "Logout",
                    tint = Red
                )
            }
        }
    ) { innerpadding ->

        // We use a Box so the announcement can overlap or sit at the top
        Box(modifier = Modifier.fillMaxSize().padding(innerpadding)) {

            Column(
                modifier = Modifier
                    .fillMaxSize()

                    .background(White),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Top,
            ) {
                // 4. ANNOUNCEMENT BANNER POSITION
                // This will appear right at the top of the dashboard content if active
                if (showAnnouncement && latestAnnouncement != null) {
                    AnnouncementBanner(
                        announcement = latestAnnouncement!!,
                        onDismiss = { showAnnouncement = false }
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                Text(
                    "User Dashboard",
                    style = TextStyle(
                        textAlign = TextAlign.Center,
                        color = Black,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 30.sp
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(50.dp))

                DashboardCard(
                    title = "Manage Bins",
                    icon = Icons.Default.Delete
                ) {
                    context.startActivity(
                        Intent(context, UserBinListActivity::class.java)
                    )
                }

                DashboardCard(
                    title = "See Live Truck",
                    icon = Icons.Default.Map
                ) {
                    context.startActivity(
                        Intent(context, UserLiveTrackingActivity::class.java)
                    )
                }

                DashboardCard(
                    title = "Payments",
                    icon = Icons.Default.Payments
                ) {
                    context.startActivity(
                        Intent(context, PaymentActivity::class.java)
                    )
                }

                DashboardCard(
                    title = "Announcement",
                    icon = Icons.Default.Announcement
                ) {
                    context.startActivity(
                    Intent(context, UserAnnouncementListActivity::class.java)
                )
                }

            }
        }
    }
}

@Preview
@Composable
fun UserDashboardPreview(){
    UserDashboardBody()
}