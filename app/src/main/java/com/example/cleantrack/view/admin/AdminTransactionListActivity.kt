package com.example.cleantrack.view.admin

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.PriceChange
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.input.KeyboardType
import com.example.cleantrack.model.SubscriptionModel
import com.example.cleantrack.repository.PaymentRepoImpl
import com.example.cleantrack.ui.theme.PrimaryGreen
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

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(PrimaryGreen, Color.White),
                    startY = 0f,
                    endY = 1000f
                )
            )
    ) {
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                CenterAlignedTopAppBar(
                    title = {
                        Text(
                            "Subscription Transactions",
                            style = TextStyle(
                                color = Color.White,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 20.sp
                            )
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = Color.White)
                        }
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = Color.Transparent
                    )
                )
            }
        ) { padding ->
            Column(modifier = Modifier.padding(padding).padding(horizontal = 16.dp)) {
                // PRICING CARD
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(4.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.PriceChange, contentDescription = null, tint = PrimaryGreen)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Subscription Pricing", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("Current monthly price: Rs. $subscriptionAmount", color = Color.Gray)
                        Spacer(modifier = Modifier.height(12.dp))
                        Button(
                            onClick = {
                                priceInput = subscriptionAmount
                                priceError = null
                                showPriceDialog = true
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Update Pricing", fontWeight = FontWeight.Bold)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                if (isLoading) {
                    Box(modifier = Modifier.fillMaxWidth().weight(1f), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = PrimaryGreen)
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxWidth().weight(1f),
                        contentPadding = PaddingValues(bottom = 20.dp)
                    ) {
                        items(transactions) { (userName, sub) ->
                            Card(
                                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                                shape = RoundedCornerShape(20.dp),
                                colors = CardDefaults.cardColors(containerColor = Color.White),
                                elevation = CardDefaults.cardElevation(2.dp)
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(
                                            modifier = Modifier.size(40.dp).background(PrimaryGreen.copy(0.1f), RoundedCornerShape(10.dp)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(Icons.Default.Payments, null, tint = PrimaryGreen)
                                        }
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Text(userName, fontWeight = FontWeight.Bold, fontSize = 17.sp)
                                        Spacer(modifier = Modifier.weight(1f))
                                        if (sub.isSubscribed) {
                                            Surface(
                                                color = Color(0xFFE8F5E9),
                                                shape = RoundedCornerShape(8.dp)
                                            ) {
                                                Text(
                                                    "ACTIVE",
                                                    color = Color(0xFF2E7D32),
                                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }
                                        }
                                    }
                                    HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), thickness = 0.5.dp, color = Color.LightGray)
                                    Text("TXN ID: ${sub.lastTransactionId}", fontSize = 13.sp, color = Color.Gray)
                                    Text("Paid on: ${sdf.format(Date(sub.startDate))}", fontSize = 14.sp)
                                    Text(
                                        "Expires: ${sdf.format(Date(sub.expiryDate))}",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (sub.isSubscribed) PrimaryGreen else Color.Red
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        if (showPriceDialog) {
            AlertDialog(
                onDismissRequest = { showPriceDialog = false },
                shape = RoundedCornerShape(24.dp),
                title = { Text("Update Price", fontWeight = FontWeight.Bold) },
                text = {
                    Column {
                        OutlinedTextField(
                            value = priceInput,
                            onValueChange = {
                                priceInput = it
                                priceError = null
                            },
                            label = { Text("Monthly price (Rs.)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = PrimaryGreen,
                                focusedLabelColor = PrimaryGreen
                            )
                        )
                        if (priceError != null) {
                            Text(priceError!!, color = Color.Red, fontSize = 12.sp, modifier = Modifier.padding(top = 4.dp))
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
                        Text("Save", fontWeight = FontWeight.Bold, color = PrimaryGreen)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showPriceDialog = false }) {
                        Text("Cancel", color = Color.Gray)
                    }
                }
            )
        }
    }
}