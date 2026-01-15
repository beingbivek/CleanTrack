package com.example.cleantrack.view.user

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Star
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
import com.example.cleantrack.repository.PaymentRepoImpl
import com.example.cleantrack.repository.UserRepoImpl
import com.example.cleantrack.viewmodel.PaymentState
import com.example.cleantrack.viewmodel.PaymentViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class PaymentActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Manual DI for simplicity
        val viewModel = PaymentViewModel(PaymentRepoImpl(), UserRepoImpl())

        setContent {
            PaymentScreen(viewModel)
        }
    }
}

@Composable
fun PaymentScreen(viewModel: PaymentViewModel) {
    val context = LocalContext.current
    val paymentState by viewModel.paymentStatus.observeAsState(PaymentState.Idle)
    val subscription by viewModel.currentSubscription.observeAsState()

    // Load status when screen opens
    LaunchedEffect(Unit) {
        viewModel.loadSubscriptionStatus()
    }

    // Handle Toasts for side effects
    LaunchedEffect(paymentState) {
        when (paymentState) {
            is PaymentState.Success -> Toast.makeText(context, (paymentState as PaymentState.Success).message, Toast.LENGTH_LONG).show()
            is PaymentState.Error -> Toast.makeText(context, (paymentState as PaymentState.Error).message, Toast.LENGTH_LONG).show()
            else -> {}
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F5))
            .padding(20.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth().padding(top = 50.dp)
        ) {
            Text(
                "CleanTrack Premium",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(30.dp))

            // 1. Subscription Status Card
            if (subscription?.isSubscribed == true) {
                ActiveSubscriptionCard(expiryDate = subscription!!.expiryDate)
            } else {
                // 2. Purchase Card
                SubscriptionOfferCard(
                    isLoading = paymentState is PaymentState.Loading,
                    onSubscribe = { viewModel.processMonthlySubscription("500") } // Fixed amount example
                )
            }
        }

        if (paymentState is PaymentState.Loading) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
        }
    }
}

@Composable
fun ActiveSubscriptionCard(expiryDate: Long) {
    val sdf = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
    val dateString = sdf.format(Date(expiryDate))

    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E9)),
        modifier = Modifier.fillMaxWidth().height(150.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(Icons.Default.CheckCircle, null, tint = Color(0xFF4CAF50), modifier = Modifier.size(48.dp))
            Spacer(modifier = Modifier.height(8.dp))
            Text("Subscription Active", fontWeight = FontWeight.Bold, fontSize = 20.sp, color = Color(0xFF2E7D32))
            Text("Valid until: $dateString", color = Color.Gray)
        }
    }
}

@Composable
fun SubscriptionOfferCard(isLoading: Boolean, onSubscribe: () -> Unit) {
    Card(
        elevation = CardDefaults.cardElevation(8.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(Icons.Default.Star, null, tint = Color(0xFFFFC107), modifier = Modifier.size(40.dp))
            Spacer(modifier = Modifier.height(16.dp))

            Text("Monthly Plan", fontSize = 24.sp, fontWeight = FontWeight.Bold)
            Text("Rs. 500 / month", fontSize = 18.sp, color = Color.Gray)

            Spacer(modifier = Modifier.height(16.dp))
            Text(
                "• Daily Waste Collection\n• Priority Support\n• Detailed Analytics",
                lineHeight = 24.sp,
                color = Color.DarkGray
            )

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = onSubscribe,
                enabled = !isLoading,
                modifier = Modifier.fillMaxWidth().height(50.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(if (isLoading) "Processing..." else "Subscribe Now")
            }
        }
    }
}