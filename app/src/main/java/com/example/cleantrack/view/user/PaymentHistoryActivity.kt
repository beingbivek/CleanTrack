package com.example.cleantrack.view.user

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.cleantrack.model.SubscriptionModel
import com.example.cleantrack.repository.PaymentRepoImpl
import com.example.cleantrack.repository.UserRepoImpl
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class PaymentHistoryActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val repo = remember { PaymentRepoImpl() }
            val userRepo = remember { UserRepoImpl() }
            PaymentHistoryScreen(repo, userRepo.getCurrentUserId() ?: "") { finish() }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PaymentHistoryScreen(repo: PaymentRepoImpl, userId: String, onBack: () -> Unit) {
    var history by remember { mutableStateOf<List<SubscriptionModel>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    val scope = rememberCoroutineScope()
    val sdf = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault())

    LaunchedEffect(Unit) {
        scope.launch {
            repo.getPaymentHistory(userId) {
                history = it
                isLoading = false
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Payment History") },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, null) } }
            )
        }
    ) { padding ->
        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else if (history.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No payment history found.", color = Color.Gray)
            }
        } else {
            LazyColumn(modifier = Modifier.padding(padding).padding(16.dp)) {
                items(history) { record ->
                    Card(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(2.dp)
                    ) {
                        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.ReceiptLong, null, tint = Color.Gray)
                            Spacer(modifier = Modifier.width(16.dp))
                            Column {
                                Text("Monthly Subscription", style = MaterialTheme.typography.titleMedium)
                                Text("TXN: ${record.lastTransactionId}", fontSize = 12.sp, color = Color.Gray)
                                Text("Date: ${sdf.format(Date(record.startDate))}", fontSize = 12.sp)
                            }
                            Spacer(modifier = Modifier.weight(1f))
                            Text("Rs. 500", fontWeight = androidx.compose.ui.text.font.FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        }
                    }
                }
            }
        }
    }
}