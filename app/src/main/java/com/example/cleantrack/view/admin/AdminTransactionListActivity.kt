package com.example.cleantrack.view.admin

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.cleantrack.model.SubscriptionModel
import com.example.cleantrack.repository.PaymentRepoImpl
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class AdminTransactionListActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            TransactionListScreen { finish() }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionListScreen(onBack: () -> Unit) {
    val repo = remember { PaymentRepoImpl() }
    var transactions by remember { mutableStateOf<List<Pair<String, SubscriptionModel>>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    val scope = rememberCoroutineScope()
    val sdf = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())

    LaunchedEffect(Unit) {
        scope.launch {
            repo.getAllTransactions {
                transactions = it
                isLoading = false
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Subscription Transactions") },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, null) }
                }
            )
        }
    ) { padding ->
        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn(modifier = Modifier.padding(padding).padding(16.dp)) {
                items(transactions) { (userName, sub) ->
                    Card(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                        elevation = CardDefaults.cardElevation(4.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Payments, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(userName, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                                Spacer(modifier = Modifier.weight(1f))
                                if (sub.isSubscribed) {
                                    Surface(color = Color(0xFFE8F5E9), shape = MaterialTheme.shapes.small) {
                                        Text("ACTIVE", color = Color(0xFF2E7D32), modifier = Modifier.padding(horizontal = 8.dp), fontSize = 12.sp)
                                    }
                                }
                            }
                            Divider(modifier = Modifier.padding(vertical = 8.dp))
                            Text("TXN ID: ${sub.lastTransactionId}", fontSize = 14.sp, color = Color.Gray)
                            Text("Paid on: ${sdf.format(Date(sub.startDate))}", fontSize = 14.sp)
                            Text("Expires: ${sdf.format(Date(sub.expiryDate))}", fontSize = 14.sp, fontWeight = FontWeight.Medium)
                        }
                    }
                }
            }
        }
    }
}