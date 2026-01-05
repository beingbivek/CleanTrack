package com.example.cleantrack.view.user

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import okhttp3.*
import java.util.*

class PaymentActivity : ComponentActivity() {

    private val client = OkHttpClient()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            // Scaffold provides basic material layout structure
            Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                Column(modifier = Modifier.padding(innerPadding)) {
                    PaymentBody { amount, onResult ->
                        initiatePayment(amount, onResult)
                    }
                }
            }
        }
    }

    private fun initiatePayment(amount: String, onResult: (String) -> Unit) {
        val remoteConfig = FirebaseRemoteConfig.getInstance()

        // We run this in IO thread for networking
        CoroutineScope(Dispatchers.IO).launch {
            try {
                // 1. AWAIT Remote Config fetch (The fix!)
                // This pauses the coroutine until keys are downloaded
                remoteConfig.fetchAndActivate().await()

                val publicKey = remoteConfig.getString("payment_public_key")
                val secretKey = remoteConfig.getString("payment_secret_key")

                if (publicKey.isEmpty() || secretKey.isEmpty()) {
                    withContext(Dispatchers.Main) {
                        onResult("Error: API Keys are empty in Remote Config")
                    }
                    return@launch
                }

                // 2. Generate random identifier
                val randomIdentifier = "TXN-" + UUID.randomUUID().toString().take(8)

                // 3. Build the request body with the fetched keys
                val formBody = FormBody.Builder()
                    .add("public_key", publicKey)
                    .add("secret_key", secretKey)
                    .add("identifier", randomIdentifier)
                    .add("currency", "NPR")
                    .add("amount", amount)
                    .add("details", "Test Payment")
                    .add("ipn_url", "http://example.com/ipn_url.php")
                    .add("success_url", "http://example.com/success_url.php")
                    .add("cancel_url", "http://example.com/cancel_url.php")
                    .add("site_name", "My Test App")
                    .add("site_logo", "http://example.com/logo.png")
                    .add("checkout_theme", "light")
                    .add("customer[first_name]", "John")
                    .add("customer[last_name]", "Doe")
                    .add("customer[email]", "john@example.com")
                    .add("customer[mobile]", "9800000000")
                    .build()

                val request = Request.Builder()
                    .url("https://apinepal.com/test/payment/initiate")
                    .post(formBody)
                    .build()

                // 4. Execute Payment Request
                val response = client.newCall(request).execute()
                val body = response.body?.string() ?: "No response"

                val resultText = if (response.isSuccessful && body.contains("\"success\"")) {
                    "Payment Success ✅\nIdentifier: $randomIdentifier"
                } else {
                    "Payment Failed ❌\n$body"
                }

                // 5. Return result to UI thread
                withContext(Dispatchers.Main) {
                    onResult(resultText)
                }

            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    onResult("Error: ${e.message}")
                }
            }
        }
    }
}

@Composable
fun PaymentBody(onProceed: (String, (String) -> Unit) -> Unit) {
    var amount by remember { mutableStateOf("") }
    var result by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text("Payment Portal", style = MaterialTheme.typography.headlineMedium)

            OutlinedTextField(
                value = amount,
                onValueChange = { amount = it },
                label = { Text("Enter Amount") },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isLoading
            )

            Button(
                onClick = {
                    if (amount.isNotEmpty()) {
                        isLoading = true
                        onProceed(amount) { res ->
                            result = res
                            isLoading = false
                        }
                    } else {
                        result = "Please enter an amount"
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isLoading
            ) {
                Text(if (isLoading) "Processing..." else "Proceed Payment")
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = result,
                style = MaterialTheme.typography.bodyLarge
            )
        }

        // Show a spinner overlay when loading
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.align(Alignment.Center)
            )
        }
    }
}