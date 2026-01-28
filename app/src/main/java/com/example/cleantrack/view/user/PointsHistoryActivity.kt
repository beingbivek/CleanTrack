package com.example.cleantrack.view.user

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Stars
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.cleantrack.model.PointsTransactionModel
import com.example.cleantrack.model.SubscriptionModel
import com.example.cleantrack.repository.PaymentRepoImpl
import com.example.cleantrack.repository.PointsRepoImpl
import com.example.cleantrack.repository.UserRepoImpl
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class PointsHistoryActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val pointsRepo = remember { PointsRepoImpl() }
            val paymentRepo = remember { PaymentRepoImpl() }
            val userRepo = remember { UserRepoImpl() }
            val userId = userRepo.getCurrentUserId() ?: ""

            PointsHistoryScreen(
                pointsRepo = pointsRepo,
                paymentRepo = paymentRepo,
                userId = userId,
                onBack = { finish() }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PointsHistoryScreen(
    pointsRepo: PointsRepoImpl,
    paymentRepo: PaymentRepoImpl,
    userId: String,
    onBack: () -> Unit
) {
    // State Management
    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("Points", "Payments")

    var pointsHistory by remember { mutableStateOf<List<PointsTransactionModel>>(emptyList()) }
    var cashHistory by remember { mutableStateOf<List<SubscriptionModel>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    val scope = rememberCoroutineScope()
    val sdf = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault())

    // Fetch Data on Load
    LaunchedEffect(Unit) {
        isLoading = true
        pointsRepo.getPointsHistory(userId) { pointsList ->
            pointsHistory = pointsList
            // Fetch payment history using a coroutine scope to avoid suspension errors
            scope.launch {
                paymentRepo.getPaymentHistory(userId) { paymentList ->
                    cashHistory = paymentList
                    isLoading = false
                }
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(Color(0xFF4CAF50), Color(0xFF81C784), Color.White)))
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // --- Top App Bar ---
            TopAppBar(
                title = { Text("Transaction History", color = Color.White, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = null, tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )

            // --- Tabs ---
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = Color.Transparent,
                contentColor = Color.White,
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                        color = Color.White
                    )
                }
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = { Text(title, color = Color.White, fontWeight = if(selectedTab == index) FontWeight.Bold else FontWeight.Normal) }
                    )
                }
            }

            // --- Content Surface ---
            Surface(
                modifier = Modifier.fillMaxSize().padding(top = 10.dp),
                shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp),
                color = Color.White
            ) {
                if (isLoading) {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = Color(0xFF4CAF50))
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        if (selectedTab == 0) {
                            if (pointsHistory.isEmpty()) {
                                item { Text("No points history found.", color = Color.Gray) }
                            } else {
                                items(pointsHistory) { transaction ->
                                    PointsItemRow(transaction, sdf)
                                }
                            }
                        } else {
                            if (cashHistory.isEmpty()) {
                                item { Text("No payment records found.", color = Color.Gray) }
                            } else {
                                items(cashHistory) { record ->
                                    CashItemRow(record, sdf)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PointsItemRow(item: PointsTransactionModel, sdf: SimpleDateFormat) {
    val isEarning = item.amount > 0
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FBF8)),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Stars, null, tint = if(isEarning) Color(0xFF4CAF50) else Color(0xFFF44336))
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(item.description, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Text(sdf.format(Date(item.timestamp)), fontSize = 12.sp, color = Color.Gray)
            }
            Text(
                text = if (isEarning) "+${item.amount} Pts" else "${item.amount} Pts",
                fontWeight = FontWeight.ExtraBold,
                color = if (isEarning) Color(0xFF2E7D32) else Color(0xFFC62828)
            )
        }
    }
}

@Composable
fun CashItemRow(record: SubscriptionModel, sdf: SimpleDateFormat) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF8F9FF)),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.CreditCard, null, tint = Color(0xFF3F51B5))
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text("Monthly Subscription", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Text("TXN: ${record.lastTransactionId.take(10)}...", fontSize = 11.sp, color = Color.Gray)
                Text(sdf.format(Date(record.startDate)), fontSize = 12.sp)
            }
            Text("Rs. 500", fontWeight = FontWeight.Bold, color = Color(0xFF3F51B5))
        }
    }
}