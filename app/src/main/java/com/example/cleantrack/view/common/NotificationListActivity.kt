package com.example.cleantrack.view.common

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.cleantrack.model.NotificationModel
import com.example.cleantrack.repository.NotificationRepoImpl
import com.example.cleantrack.ui.theme.*
import com.example.cleantrack.util.NotificationTypes
import com.example.cleantrack.view.admin.AdminContactSupportViewActivity
import com.example.cleantrack.view.admin.AdminTransactionListActivity
import com.example.cleantrack.view.driver.DriverDashboardActivity
import com.example.cleantrack.view.driver.DriverRoutineActivity
import com.example.cleantrack.view.user.PaymentHistoryActivity
import com.example.cleantrack.view.user.UserAnnouncementListActivity
import com.example.cleantrack.view.user.UserBinListActivity
import com.example.cleantrack.view.user.UserRouteLiveTrackingActivity
import com.example.cleantrack.view.user.UserScheduleListActivity
import com.example.cleantrack.viewmodel.NotificationViewModel
import com.google.firebase.auth.FirebaseAuth
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.apply
import kotlin.jvm.java
import kotlin.text.equals
import kotlin.text.isNotBlank

class NotificationListActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val role = intent.getStringExtra("ROLE") ?: "USER"
        setContent { NotificationListScreen(role) }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationListScreen(role: String) {
    val context = LocalContext.current
    val viewModel = remember { NotificationViewModel(NotificationRepoImpl()) }
    val notifications by viewModel.notifications.observeAsState(emptyList())
    val loading by viewModel.loading.observeAsState(false)
    val userId = FirebaseAuth.getInstance().currentUser?.uid ?: ""

    LaunchedEffect(userId) {
        if (userId.isNotBlank()) {
            viewModel.loadNotifications(userId)
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Notifications", color = White, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { (context as? Activity)?.finish() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = White)
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Green)
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(Color(0xFFF5F6FA))
        ) {
            when {
                loading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center), color = Green)
                }
                notifications.isEmpty() -> {
                    Column(
                        modifier = Modifier.align(Alignment.Center),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(Icons.Default.Notifications, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(48.dp))
                        Spacer(modifier = Modifier.height(12.dp))
                        Text("No notifications yet", color = Color.Gray)
                    }
                }
                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(notifications) { notification ->
                            NotificationCard(
                                notification = notification,
                                onClick = {
                                    viewModel.markAsRead(userId, notification.notificationId)
                                    handleNotificationTap(context, role, userId, notification)
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun NotificationCard(notification: NotificationModel, onClick: () -> Unit) {
    val timeStamp = remember(notification.createdAt) {
        SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault()).format(Date(notification.createdAt))
    }
    val unreadColor = if (notification.read) White else Color(0xFFE8F5E9)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = unreadColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(notification.title, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Black)
            Spacer(modifier = Modifier.height(6.dp))
            Text(notification.message, fontSize = 14.sp, color = Color.DarkGray)
            Spacer(modifier = Modifier.height(10.dp))
            Text(timeStamp, fontSize = 11.sp, color = Color.Gray)
        }
    }
}

private fun handleNotificationTap(
    context: Context,
    role: String,
    userId: String,
    notification: NotificationModel
) {
    when (notification.type) {
        NotificationTypes.ANNOUNCEMENT -> {
            context.startActivity(Intent(context, UserAnnouncementListActivity::class.java))
        }
        NotificationTypes.ROUTE_STARTED,
        NotificationTypes.ROUTE_ENDED -> {
            if (role.equals("DRIVER", ignoreCase = true)) {
                context.startActivity(Intent(context, DriverDashboardActivity::class.java))
            } else {
                context.startActivity(Intent(context, UserRouteLiveTrackingActivity::class.java))
            }
        }
        NotificationTypes.SCHEDULE_ADDED,
        NotificationTypes.SCHEDULE_UPDATED,
        NotificationTypes.SCHEDULE_DELETED,
        NotificationTypes.SCHEDULE_ARRIVED -> {
            if (role.equals("DRIVER", ignoreCase = true)) {
                context.startActivity(Intent(context, DriverRoutineActivity::class.java))
            } else {
                context.startActivity(Intent(context, UserScheduleListActivity::class.java))
            }
        }
        NotificationTypes.BID_PLACED,
        NotificationTypes.BID_WON -> {
            val productId = notification.metadata["productId"] ?: ""
            context.startActivity(Intent(context, ProductDetailActivity::class.java).apply {
                putExtra("PRODUCT_ID", productId)
                putExtra("USER_ID", userId)
            })
        }
        NotificationTypes.POLICY_UPDATED -> {
            context.startActivity(Intent(context, PrivacyPolicyActivity::class.java))
        }
        NotificationTypes.TERMS_UPDATED -> {
            context.startActivity(Intent(context, TermsAndConditionActivity::class.java))
        }
        NotificationTypes.BIN_RATED -> {
            context.startActivity(Intent(context, UserBinListActivity::class.java))
        }
        NotificationTypes.SUPPORT_TICKET_CREATED -> {
            if (role.equals("ADMIN", ignoreCase = true)) {
                context.startActivity(Intent(context, AdminContactSupportViewActivity::class.java))
            } else {
                context.startActivity(Intent(context, IssuesViewActivity::class.java))
            }
        }
        NotificationTypes.SUPPORT_TICKET_REPLY -> {
            context.startActivity(Intent(context, IssuesViewActivity::class.java))
        }
        NotificationTypes.SUBSCRIPTION_ACTIVATED -> {
            if (role.equals("ADMIN", ignoreCase = true)) {
                context.startActivity(Intent(context, AdminTransactionListActivity::class.java))
            } else {
                context.startActivity(Intent(context, PaymentHistoryActivity::class.java))
            }
        }
    }
}
