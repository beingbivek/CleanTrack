package com.example.cleantrack.view.user

import android.app.Activity
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Stars
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
import com.example.cleantrack.repository.PointsRepoImpl
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
    private lateinit var userViewModel: UserViewModel
    private lateinit var paymentSheet: PaymentSheet
    private var pendingTransactionId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val userId = intent.getStringExtra("USER_ID") ?: ""

        userViewModel = UserViewModel(UserRepoImpl())
        // UPDATED: Initializing PaymentViewModel with PointsRepoImpl
        paymentViewModel = PaymentViewModel(PaymentRepoImpl(), UserRepoImpl(), PointsRepoImpl())

        paymentSheet = PaymentSheet(this) { result ->
            when (result) {
                is PaymentSheetResult.Completed -> {
                    pendingTransactionId?.let { tid ->
                        paymentViewModel.completePayment(userId, tid)
                    } ?: run {
                        paymentViewModel.resetPaymentStatus()
                        Toast.makeText(this, "Transaction Error", Toast.LENGTH_SHORT).show()
                    }
                }
                is PaymentSheetResult.Canceled -> {
                    paymentViewModel.resetPaymentStatus()
                }
                is PaymentSheetResult.Failed -> {
                    paymentViewModel.resetPaymentStatus()
                    Toast.makeText(this, "Payment Failed: ${result.error.localizedMessage}", Toast.LENGTH_LONG).show()
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
                        PaymentSheet.Configuration(
                            merchantDisplayName = "CleanTrack",
                            allowsDelayedPaymentMethods = false
                        )
                    )
                }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
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
            paymentViewModel.loadSubscriptionAmount()
        }
    }

    LaunchedEffect(paymentState) {
        when (val state = paymentState) {
            is PaymentState.LaunchStripe -> {
                try {
                    val remoteConfig = FirebaseRemoteConfig.getInstance()
                    remoteConfig.fetchAndActivate().await()
                    val publicKey = remoteConfig.getString("stripe_public_key").trim()

                    if (publicKey.isNotEmpty()) {
                        PaymentConfiguration.init(context, publicKey)
                        onLaunchPayment(state.clientSecret, state.paymentIntentId)
                    } else {
                        Toast.makeText(context, "Config Error: Key missing", Toast.LENGTH_SHORT).show()
                        paymentViewModel.resetPaymentStatus()
                    }
                } catch (e: Exception) {
                    paymentViewModel.resetPaymentStatus()
                }
            }
            is PaymentState.Success -> {
                Toast.makeText(context, state.message, Toast.LENGTH_LONG).show()
                userViewModel.getUserById(userId)
                notificationViewModel.notifyUser(userId, NotificationPayload(
                    title = "Subscription active",
                    message = "Your premium features are unlocked.",
                    type = "subscription", actionType = "subscription"
                ))
                paymentViewModel.resetPaymentStatus()
            }
            is PaymentState.Error -> {
                Toast.makeText(context, state.message, Toast.LENGTH_SHORT).show()
                paymentViewModel.resetPaymentStatus()
            }
            else -> Unit
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(brush = Brush.verticalGradient(colors = listOf(Blue.copy(alpha = 0.8f), Green.copy(alpha = 0.4f), Color.White)))
    ) {
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                CenterAlignedTopAppBar(
                    title = { Text("CleanTrack Premium", fontWeight = FontWeight.ExtraBold, color = Color.White) },
                    navigationIcon = {
                        IconButton(onClick = { (context as? Activity)?.finish() }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                        }
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.Transparent)
                )
            }
        ) { padding ->
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(padding)
                    .padding(horizontal = 24.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Spacer(modifier = Modifier.height(30.dp))
                Text("Upgrade Your Experience", fontSize = 14.sp, color = Color.White, fontWeight = FontWeight.Medium)
                Spacer(modifier = Modifier.height(30.dp))

                if (isUserLoading && userProfile == null) {
                    Box(modifier = Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = Color.White)
                    }
                } else {
                    val isPremium = userViewModel.isPremiumUser(userProfile)

                    if (isPremium) {
                        ActiveSubscriptionCard(expiryDate = userProfile?.subscription?.expiryDate ?: 0L)
                    } else {
                        // 1. Existing Stripe Offer Card
                        SubscriptionOfferCard(
                            isLoading = paymentState is PaymentState.Loading,
                            amount = subscriptionAmount.ifBlank { "500" },
                            onSubscribe = {
                                paymentViewModel.processMonthlySubscription(subscriptionAmount.ifBlank { "500" })
                            }
                        )

                        Spacer(modifier = Modifier.height(20.dp))

                        // 2. NEW: Points Redemption Card
                        PointsRedemptionCard(
                            isLoading = paymentState is PaymentState.Loading,
                            pointsNeeded = subscriptionAmount.ifBlank { "500" }.toInt(),
                            onRedeem = { points ->
                                paymentViewModel.processPointsPayment(points)
                            }
                        )
                    }
                }
                Spacer(modifier = Modifier.height(40.dp))
            }
        }

        if (paymentState is PaymentState.Loading) {
            Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(0.4f)), contentAlignment = Alignment.Center) {
                Card(colors = CardDefaults.cardColors(containerColor = Color.White), shape = RoundedCornerShape(12.dp)) {
                    Row(Modifier.padding(24.dp), verticalAlignment = Alignment.CenterVertically) {
                        CircularProgressIndicator(color = Green, modifier = Modifier.size(24.dp))
                        Spacer(Modifier.width(16.dp))
                        Text("Processing...", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun PointsRedemptionCard(isLoading: Boolean, pointsNeeded: Int, onRedeem: (Int) -> Unit) {
    Card(
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.cardElevation(4.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF1F8E9)), // Light Greenish background
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Stars, contentDescription = null, tint = Color(0xFFFBC02D))
                Spacer(modifier = Modifier.width(8.dp))
                Text("REWARDS REDEMPTION", color = Color(0xFF2E7D32), fontWeight = FontWeight.Bold, fontSize = 11.sp)
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text("Pay with Points", fontSize = 22.sp, fontWeight = FontWeight.ExtraBold, color = Color.Black)
            Text("Use $pointsNeeded reward points to pay your fee", color = Color.Gray, fontSize = 13.sp)

            Spacer(modifier = Modifier.height(20.dp))

            Button(
                onClick = { onRedeem(pointsNeeded) },
                enabled = !isLoading,
                modifier = Modifier.fillMaxWidth().height(50.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32))
            ) {
                Text("Redeem $pointsNeeded Points", fontWeight = FontWeight.Bold)
            }
        }
    }
}

// --- EXISTING CARDS REMAIN UNCHANGED ---

@Composable
fun ActiveSubscriptionCard(expiryDate: Long) {
    val sdf = remember { SimpleDateFormat("dd MMMM yyyy", Locale.getDefault()) }
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
            Text("Premium Active", fontWeight = FontWeight.ExtraBold, fontSize = 22.sp, color = Green)
            Text("All features are unlocked", color = Color.Gray, fontSize = 14.sp)
            HorizontalDivider(Modifier.padding(vertical = 24.dp), thickness = 1.dp, color = Color(0xFFF0F0F0))
            Text("Valid until: $dateString", fontSize = 14.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun SubscriptionOfferCard(isLoading: Boolean, amount: String, onSubscribe: () -> Unit) {
    Card(
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.cardElevation(8.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Surface(color = Color(0xFFFFD700).copy(0.1f), shape = RoundedCornerShape(8.dp)) {
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
                if (isLoading) CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp))
                else Text("Upgrade to Premium", fontWeight = FontWeight.Bold)
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