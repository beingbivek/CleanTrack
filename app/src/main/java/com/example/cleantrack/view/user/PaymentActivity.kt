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
import com.example.cleantrack.repository.PaymentRepoImpl
import com.example.cleantrack.repository.UserRepoImpl
import com.example.cleantrack.ui.theme.Green
import com.example.cleantrack.ui.theme.Blue
import com.example.cleantrack.viewmodel.PaymentState
import com.example.cleantrack.viewmodel.PaymentViewModel
import com.example.cleantrack.viewmodel.UserViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class PaymentActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // 1. Get the ID passed from Dashboard
        val userId = intent.getStringExtra("USER_ID") ?: ""

        // 2. Initialize both ViewModels
        // We use UserViewModel to fetch the profile by ID
        // We use PaymentViewModel to handle the "Upgrade" button clicks
        val userViewModel = UserViewModel(UserRepoImpl())
        val paymentViewModel = PaymentViewModel(PaymentRepoImpl(), UserRepoImpl())

        setContent {
            PaymentScreen(userViewModel, paymentViewModel, userId)
        }
    }
}

@Composable
fun PaymentScreen(
    userViewModel: UserViewModel,
    paymentViewModel: PaymentViewModel,
    userId: String
) {
    val context = LocalContext.current

    // Observe the User object from UserViewModel
    val userProfile by userViewModel.user.observeAsState()
    val isUserLoading by userViewModel.loading.observeAsState(true)

    // Observe Payment state for the "Processing" overlay
    val paymentState by paymentViewModel.paymentStatus.observeAsState(PaymentState.Idle)

    // 3. Trigger the fetch using the ID from Intent immediately on launch
    LaunchedEffect(userId) {
        if (userId.isNotEmpty()) {
            userViewModel.getUserById(userId)
        }
    }

    // Refresh user profile after a successful payment
    LaunchedEffect(paymentState) {
        if (paymentState is PaymentState.Success) {
            Toast.makeText(context, (paymentState as PaymentState.Success).message, Toast.LENGTH_LONG).show()
            userViewModel.getUserById(userId)
        } else if (paymentState is PaymentState.Error) {
            Toast.makeText(context, (paymentState as PaymentState.Error).message, Toast.LENGTH_LONG).show()
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

            // 4. LOGIC CHECK using your UserViewModel's helper function
            if (isUserLoading) {
                Box(modifier = Modifier.fillMaxWidth().height(300.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Green)
                }
            } else {
                // Use the precise logic from your ViewModel
                val isPremium = userViewModel.isPremiumUser(userProfile)

                if (isPremium) {
                    // Extract expiry from nested model as you described
                    val expiryDate = userProfile?.subscription?.expiryDate ?: userProfile?.expiryDate ?: 0L
                    ActiveSubscriptionCard(expiryDate = expiryDate)
                } else {
                    SubscriptionOfferCard(
                        isLoading = paymentState is PaymentState.Loading,
                        onSubscribe = { paymentViewModel.processMonthlySubscription("500") }
                    )
                }
            }

            Spacer(modifier = Modifier.height(30.dp))
        }

        // Processing Overlay
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

// ... ActiveSubscriptionCard, SubscriptionOfferCard, and BenefitItem remain the same ...

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
fun SubscriptionOfferCard(isLoading: Boolean, onSubscribe: () -> Unit) {
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
            Text("Rs. 500", fontSize = 36.sp, fontWeight = FontWeight.Black)
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