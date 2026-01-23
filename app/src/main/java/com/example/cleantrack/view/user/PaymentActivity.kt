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
import com.example.cleantrack.model.NotificationPayload
import com.example.cleantrack.repository.NotificationRepoImpl
import com.example.cleantrack.repository.PaymentRepoImpl
import com.example.cleantrack.repository.UserRepoImpl
import com.example.cleantrack.ui.theme.Green
import com.example.cleantrack.ui.theme.Blue
import com.example.cleantrack.viewmodel.PaymentState
import com.example.cleantrack.viewmodel.PaymentViewModel
import com.example.cleantrack.viewmodel.UserViewModel
import com.example.cleantrack.viewmodel.NotificationViewModel
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.stripe.android.PaymentConfiguration
import com.stripe.android.paymentsheet.PaymentSheet
import com.stripe.android.paymentsheet.PaymentSheetResult
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class PaymentActivity : ComponentActivity() {
    private lateinit var paymentViewModel: PaymentViewModel
    private lateinit var paymentSheet: PaymentSheet
    private var pendingTransactionId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val userId = intent.getStringExtra("USER_ID") ?: ""
        val userViewModel = UserViewModel(UserRepoImpl())
        paymentViewModel = PaymentViewModel(PaymentRepoImpl(), UserRepoImpl())
        paymentSheet = PaymentSheet(this) { result ->
            when (result) {
                is PaymentSheetResult.Completed -> {
                    val transactionId = pendingTransactionId
                    if (!transactionId.isNullOrBlank()) {
                        paymentViewModel.completePayment(userId, transactionId)
                    } else {
                        paymentViewModel.resetPaymentStatus()
                        Toast.makeText(this, "Payment completed, but transaction ID missing.", Toast.LENGTH_LONG).show()
                    }
                }
                is PaymentSheetResult.Canceled -> {
                    paymentViewModel.resetPaymentStatus()
                }
                is PaymentSheetResult.Failed -> {
                    paymentViewModel.resetPaymentStatus()
                    Toast.makeText(this, result.error.localizedMessage ?: "Payment failed.", Toast.LENGTH_LONG).show()
                }
            }
        }

        setContent {
            PaymentScreen(
                userViewModel = userViewModel,
                paymentViewModel = paymentViewModel,
                userId = userId,
                onLaunchPayment = { clientSecret, transactionId ->
                    pendingTransactionId = transactionId
                    paymentSheet.presentWithPaymentIntent(
                        clientSecret,
                        PaymentSheet.Configuration(merchantDisplayName = "CleanTrack")
                    )
                }
            )
        }
    }
}

@Composable
fun PaymentScreen(
    userViewModel: UserViewModel,
    paymentViewModel: PaymentViewModel,
    userId: String,
    onLaunchPayment: (clientSecret: String, transactionId: String) -> Unit
) {
    val context = LocalContext.current
    val userProfile by userViewModel.user.observeAsState()
    val isUserLoading by userViewModel.loading.observeAsState(true)
    val paymentState by paymentViewModel.paymentStatus.observeAsState(PaymentState.Idle)
    val subscriptionAmount by paymentViewModel.subscriptionAmount.observeAsState("500")
    val notificationViewModel = remember { NotificationViewModel(NotificationRepoImpl(), UserRepoImpl()) }

    LaunchedEffect(userId) {
        if (userId.isNotEmpty()) {
            userViewModel.getUserById(userId)
        }
    }

    LaunchedEffect(Unit) {
        paymentViewModel.loadSubscriptionAmount()
    }

    LaunchedEffect(paymentState) {
        when (paymentState) {
            is PaymentState.LaunchStripe -> {
                val launchState = paymentState as PaymentState.LaunchStripe
                val remoteConfig = FirebaseRemoteConfig.getInstance()
                remoteConfig.fetchAndActivate().await()
                val publicKey = remoteConfig.getString("stripe_public_key").trim()
                if (publicKey.isBlank()) {
                    Toast.makeText(context, "Payment public key is missing.", Toast.LENGTH_LONG).show()
                    return@LaunchedEffect
                }

                PaymentConfiguration.init(context, publicKey)
                onLaunchPayment(launchState.clientSecret, launchState.paymentIntentId)
            }
            is PaymentState.Success -> {
                Toast.makeText(context, (paymentState as PaymentState.Success).message, Toast.LENGTH_LONG).show()
                userViewModel.getUserById(userId)
                notificationViewModel.notifyUser(
                    userId,
                    NotificationPayload(
                        title = "Subscription active",
                        message = "Your subscription is now active.",
                        type = "subscription",
                        actionType = "subscription"
                    )
                )
                notificationViewModel.notifyAllAdmins(
                    NotificationPayload(
                        title = "New subscription",
                        message = "${userProfile?.fullname ?: "A user"} subscribed.",
                        type = "subscription",
                        actionType = "subscription"
                    )
                )
                paymentViewModel.resetPaymentStatus()
            }
            is PaymentState.Error -> {
                Toast.makeText(context, (paymentState as PaymentState.Error).message, Toast.LENGTH_LONG).show()
                paymentViewModel.resetPaymentStatus()
            }
            else -> Unit
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
            Text("Advanced Waste Management Features", fontSize = 14.sp, color = Color.Gray)
            Spacer(modifier = Modifier.height(40.dp))

            if (isUserLoading) {
                Box(modifier = Modifier.fillMaxWidth().height(300.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Green)
                }
            } else {
                val isPremium = userViewModel.isPremiumUser(userProfile)

                if (isPremium) {
                    val expiryDate = userProfile?.subscription?.expiryDate ?: 0L
                    ActiveSubscriptionCard(expiryDate = expiryDate)
                } else {
                    val normalizedAmount = subscriptionAmount.ifBlank { "500" }
                    SubscriptionOfferCard(
                        isLoading = paymentState is PaymentState.Loading,
                        amount = normalizedAmount,
                        onSubscribe = { paymentViewModel.processMonthlySubscription(normalizedAmount) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(30.dp))
        }

        if (paymentState is PaymentState.Loading) {
            Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(0.4f)), contentAlignment = Alignment.Center) {
                Card(colors = CardDefaults.cardColors(containerColor = Color.White), shape = RoundedCornerShape(12.dp)) {
                    Row(Modifier.padding(24.dp), verticalAlignment = Alignment.CenterVertically) {
                        CircularProgressIndicator(color = Green, modifier = Modifier.size(24.dp))
                        Spacer(Modifier.width(16.dp))
                        Text("Authorizing...", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun ActiveSubscriptionCard(expiryDate: Long) {
    val sdf = SimpleDateFormat("dd MMMM yyyy", Locale.getDefault())
    val dateString = if (expiryDate > 0) sdf.format(Date(expiryDate)) else "Lifetime"

    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(6.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(32.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(Icons.Default.Verified, null, tint = Green, modifier = Modifier.size(70.dp))
            Spacer(modifier = Modifier.height(16.dp))
            Text("Premium Active", fontWeight = FontWeight.ExtraBold, fontSize = 24.sp, color = Green)
            Text("All features are unlocked", color = Color.Gray, fontSize = 14.sp)
            HorizontalDivider(Modifier.padding(vertical = 24.dp), thickness = 1.dp, color = Color(0xFFF0F0F0))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Valid until: ", fontSize = 14.sp)
                Text(dateString, color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            }
        }
    }
}

@Composable
fun SubscriptionOfferCard(isLoading: Boolean, amount: String, onSubscribe: () -> Unit) {
    Card(
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.cardElevation(10.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Surface(color = Color(0xFFFFD700).copy(0.15f), shape = RoundedCornerShape(8.dp)) {
                Text("MONTHLY PLAN", modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp), color = Color(0xFF856404), fontWeight = FontWeight.Bold, fontSize = 11.sp)
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text("Rs. $amount", fontSize = 36.sp, fontWeight = FontWeight.Black)
            Text("Billed every 30 days", color = Color.Gray, fontSize = 13.sp)
            Spacer(modifier = Modifier.height(24.dp))
            BenefitItem("Live Truck Tracking")
            BenefitItem("Collection Schedules")
            BenefitItem("Smart AI Waste Review")
            Spacer(modifier = Modifier.height(32.dp))
            Button(
                onClick = onSubscribe,
                enabled = !isLoading,
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Green)
            ) {
                Text("Upgrade to Premium", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
        }
    }
}

@Composable
fun BenefitItem(text: String) {
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
        Icon(Icons.Default.CheckCircle, null, tint = Green, modifier = Modifier.size(20.dp))
        Spacer(modifier = Modifier.width(12.dp))
        Text(text, fontSize = 14.sp, color = Color.DarkGray)
    }
}
