package com.example.cleantrack.view.user

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Route
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.cleantrack.repository.PaymentRepoImpl
import com.example.cleantrack.repository.UserRepoImpl
import com.example.cleantrack.ui.theme.Green
import com.example.cleantrack.ui.theme.Blue
import com.example.cleantrack.viewmodel.PaymentState
import com.example.cleantrack.viewmodel.PaymentViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class PaymentActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

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

    LaunchedEffect(Unit) {
        viewModel.loadSubscriptionStatus()
    }

    LaunchedEffect(paymentState) {
        when (paymentState) {
            is PaymentState.Success -> {
                Toast.makeText(context, (paymentState as PaymentState.Success).message, Toast.LENGTH_LONG).show()
                // OPTIONAL: Automatically close the payment screen after success
                // (context as? Activity)?.finish()
            }
            is PaymentState.Error -> Toast.makeText(context, (paymentState as PaymentState.Error).message, Toast.LENGTH_LONG).show()
            else -> {}
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(brush = Brush.verticalGradient(colors = listOf(Blue.copy(alpha = 0.1f), Color.White)))
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(modifier = Modifier.height(80.dp))

            Text("CleanTrack Premium", fontSize = 28.sp, fontWeight = FontWeight.ExtraBold, color = Green)
            Text("Unlock full waste management power", fontSize = 14.sp, color = Color.Gray)

            Spacer(modifier = Modifier.height(40.dp))

            // --- REFACTORED SUBSCRIPTION CHECK ---
            val currentTime = System.currentTimeMillis()
            // Safe access: uses the new top-level expiryDate we added to UserModel
            val userExpiry = subscription?.expiryDate ?: 0L
            val isActive = subscription?.isSubscribed == true && userExpiry > currentTime

            if (isActive) {
                ActiveSubscriptionCard(expiryDate = userExpiry)
            } else {
                SubscriptionOfferCard(
                    isLoading = paymentState is PaymentState.Loading,
                    onSubscribe = { viewModel.processMonthlySubscription("500") }
                )
            }
            // ---------------------------------------

            Spacer(modifier = Modifier.height(30.dp))
        }

        if (paymentState is PaymentState.Loading) {
            Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(0.2f)), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Green)
            }
        }
    }
}

@Composable
fun ActiveSubscriptionCard(expiryDate: Long) {
    val sdf = SimpleDateFormat("dd MMMM yyyy", Locale.getDefault())
    val dateString = sdf.format(Date(expiryDate))

    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(4.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(Icons.Default.Verified, null, tint = Green, modifier = Modifier.size(60.dp))
            Spacer(modifier = Modifier.height(16.dp))
            Text("You are a Pro Member", fontWeight = FontWeight.Bold, fontSize = 20.sp)
            Spacer(modifier = Modifier.height(8.dp))
            Text("Your premium benefits are active.", color = Color.Gray)
            Text("Valid until: $dateString", fontWeight = FontWeight.SemiBold, color = Color.Black)

            Spacer(modifier = Modifier.height(24.dp))

            OutlinedButton(
                onClick = { /* Could implement renewal or cancel logic */ },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Manage Subscription", color = Green)
            }
        }
    }
}

@Composable
fun SubscriptionOfferCard(isLoading: Boolean, onSubscribe: () -> Unit) {
    Card(
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.cardElevation(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Surface(
                color = Color(0xFFFFD700).copy(alpha = 0.2f),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    "BEST VALUE",
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                    color = Color(0xFFB8860B),
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
            Text("Rs. 500", fontSize = 36.sp, fontWeight = FontWeight.Black)
            Text("per month", color = Color.Gray)

            Spacer(modifier = Modifier.height(32.dp))

            // Updated benefits list to match your dashboard locks
            BenefitItem("Access to Live Routes & Tracking")
            BenefitItem("Automated Collection Schedule")
            BenefitItem("Advanced Bin Management")
            BenefitItem("Priority AI Analysis & Feedback")
            BenefitItem("Ad-free Marketplace Posting")

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = onSubscribe,
                enabled = !isLoading,
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Green)
            ) {
                Text(
                    if (isLoading) "Securing Payment..." else "Upgrade to Premium",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun BenefitItem(text: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(Icons.Default.CheckCircle, null, tint = Green, modifier = Modifier.size(20.dp))
        Spacer(modifier = Modifier.width(12.dp))
        Text(text, fontSize = 14.sp, color = Color.DarkGray)
    }
}