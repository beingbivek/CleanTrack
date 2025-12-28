package com.example.cleantrack.view.user

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.*
import java.util.*

class PaymentActivity : ComponentActivity() {

    private val client = OkHttpClient()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            PaymentBody { amount, onResult ->
                initiatePayment(amount, onResult)
            }
        }
    }

    private fun initiatePayment(amount: String, onResult: (String) -> Unit) {
        var public_key = ""
        var secret_key = ""
        val remoteConfig = FirebaseRemoteConfig.getInstance()
        remoteConfig.fetchAndActivate().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                public_key = remoteConfig.getString("payment_public_key")
                secret_key = remoteConfig.getString("payment_secret_key")
            }
        }

        // Generate random identifier
        val randomIdentifier = "TXN-" + UUID.randomUUID().toString().take(8)

        val formBody = FormBody.Builder()
            .add("public_key", public_key)   // replace with your test public key
            .add("secret_key", secret_key)   // replace with your test secret key
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

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = client.newCall(request).execute()
                val body = response.body?.string() ?: "No response"
                val result = if (response.isSuccessful && body.contains("\"success\"")) {
                    "Payment Success ✅\nIdentifier: $randomIdentifier\n$body"
                } else {
                    "Payment Failed ❌\nIdentifier: $randomIdentifier\n$body"
                }
                onResult(result)
            } catch (e: Exception) {
                onResult("Error: ${e.message}")
            }
        }
    }
}

@Composable
fun PaymentBody(onProceed: (String, (String) -> Unit) -> Unit) {
    var amount by remember { mutableStateOf("") }
    var result by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        OutlinedTextField(
            value = amount,
            onValueChange = { amount = it },
            label = { Text("Enter Amount") },
            modifier = Modifier.fillMaxWidth()
        )

        Button(
            onClick = {
                if (amount.isNotEmpty()) {
                    onProceed(amount) { res -> result = res }
                } else {
                    result = "Please enter an amount"
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Proceed Payment")
        }

        Text(
            text = result,
            style = MaterialTheme.typography.bodyLarge
        )
    }
}
