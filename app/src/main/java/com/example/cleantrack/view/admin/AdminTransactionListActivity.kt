package com.example.cleantrack.view.admin

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.PriceChange
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.input.KeyboardType
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
    var subscriptionAmount by remember { mutableStateOf("500") }
    var showPriceDialog by remember { mutableStateOf(false) }
    var priceInput by remember { mutableStateOf("") }
    var priceError by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()
    val sdf = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())

    LaunchedEffect(Unit) {
        scope.launch {
            repo.getAllTransactions {
                transactions = it
                isLoading = false
            }
        }
        scope.launch {
            val result = repo.getSubscriptionAmount()
            subscriptionAmount = result.getOrDefault("500")
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
        Column(modifier = Modifier.padding(padding).padding(16.dp)) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(4.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.PriceChange, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Subscription Pricing", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Current monthly price: Rs. $subscriptionAmount", color = Color.Gray)
                    Spacer(modifier = Modifier.height(12.dp))
                    Button(onClick = {
                        priceInput = subscriptionAmount
                        priceError = null
                        showPriceDialog = true
                    }) {
                        Text("Update Pricing")
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (isLoading) {
                Box(modifier = Modifier.fillMaxWidth().padding(vertical = 32.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else {
                LazyColumn(modifier = Modifier.fillMaxWidth().weight(1f)) {
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

        if (showPriceDialog) {
            AlertDialog(
                onDismissRequest = { showPriceDialog = false },
                title = { Text("Update Subscription Price") },
                text = {
                    Column {
                        OutlinedTextField(
                            value = priceInput,
                            onValueChange = {
                                priceInput = it
                                priceError = null
                            },
                            label = { Text("Monthly price (Rs.)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                        )
                        if (priceError != null) {
                            Text(priceError!!, color = MaterialTheme.colorScheme.error, fontSize = 12.sp)
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = {
                        val sanitized = priceInput.trim()
                        if (sanitized.isBlank()) {
                            priceError = "Price cannot be empty."
                            return@TextButton
                        }
                        if (sanitized.toDoubleOrNull() == null) {
                            priceError = "Enter a valid number."
                            return@TextButton
                        }
                        scope.launch {
                            val result = repo.updateSubscriptionAmount(sanitized)
                            if (result.isSuccess) {
                                subscriptionAmount = sanitized
                                showPriceDialog = false
                            } else {
                                priceError = result.exceptionOrNull()?.message ?: "Failed to update pricing."
                            }
                        }
                    }) {
                        Text("Save")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showPriceDialog = false }) {
                        Text("Cancel")
                    }
                }
            )
        }
    }
}
