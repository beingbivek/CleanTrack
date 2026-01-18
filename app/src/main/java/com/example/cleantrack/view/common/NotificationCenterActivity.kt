package com.example.cleantrack.view.common

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.cleantrack.model.NotificationModel
import com.example.cleantrack.repository.NotificationRepoImpl
import com.example.cleantrack.repository.UserRepoImpl
import com.example.cleantrack.ui.theme.Green
import com.example.cleantrack.ui.theme.White
import com.example.cleantrack.view.common.MarketplaceActivity
import com.example.cleantrack.view.common.ProductDetailActivity
import com.example.cleantrack.view.common.IssuesViewActivity
import com.example.cleantrack.view.common.PrivacyPolicyActivity
import com.example.cleantrack.view.common.TermsAndConditionActivity
import com.example.cleantrack.view.user.UserAnnouncementListActivity
import com.example.cleantrack.view.user.UserScheduleListActivity
import com.example.cleantrack.viewmodel.NotificationViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class NotificationCenterActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { NotificationCenterScreen { finish() } }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationCenterScreen(onBack: () -> Unit) {
    val viewModel = remember { NotificationViewModel(NotificationRepoImpl(), UserRepoImpl()) }
    val currentUserId = UserRepoImpl().getCurrentUserId() ?: ""
    val notifications by viewModel.notifications.observeAsState(emptyList())
    val context = LocalContext.current

    LaunchedEffect(currentUserId) {
        if (currentUserId.isNotEmpty()) viewModel.observeNotifications(currentUserId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Notifications", color = White) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = null, tint = White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Green)
            )
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding).background(Color(0xFFF7F7F7))) {
            if (notifications.isEmpty()) {
                Column(modifier = Modifier.align(Alignment.Center), horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.Notifications, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(48.dp))
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("No notifications yet", color = Color.Gray)
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(notifications) { item ->
                        NotificationCard(notification = item) {
                            viewModel.markAsRead(currentUserId, item.notificationId) { _, _ -> }
                            handleNotificationTap(context, item)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun NotificationCard(notification: NotificationModel, onClick: () -> Unit) {
    val timestamp = remember(notification.timestamp) {
        SimpleDateFormat("dd MMM, HH:mm", Locale.getDefault()).format(Date(notification.timestamp))
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = if (notification.read) White else Color(0xFFE8F5E9)),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(notification.title, fontWeight = FontWeight.Bold, color = Color.Black)
                Text(timestamp, style = MaterialTheme.typography.labelSmall, color = Color.Gray)
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(notification.message, color = Color.DarkGray)
        }
    }
}

private fun handleNotificationTap(context: Context, notification: NotificationModel) {
    when (notification.actionType) {
        "announcement" -> context.startActivity(Intent(context, UserAnnouncementListActivity::class.java))
        "schedule" -> context.startActivity(Intent(context, UserScheduleListActivity::class.java))
        "privacy" -> context.startActivity(Intent(context, PrivacyPolicyActivity::class.java))
        "terms" -> context.startActivity(Intent(context, TermsAndConditionActivity::class.java))
        "marketplace" -> context.startActivity(Intent(context, MarketplaceActivity::class.java))
        "product_detail" -> {
            val intent = Intent(context, ProductDetailActivity::class.java).apply {
                putExtra("PRODUCT_ID", notification.productId)
                putExtra("USER_ID", UserRepoImpl().getCurrentUserId())
            }
            context.startActivity(intent)
        }
        "ticket_detail" -> context.startActivity(Intent(context, IssuesViewActivity::class.java))
        else -> Unit
    }
}
