package com.example.cleantrack.repository

import android.util.Log
import com.example.cleantrack.model.SubscriptionModel
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import kotlinx.coroutines.tasks.await
import okhttp3.FormBody
import okhttp3.OkHttpClient
import okhttp3.Request
import java.util.Calendar
import java.util.UUID

class PaymentRepoImpl : PaymentRepo {

    private val client = OkHttpClient()
    private val db = FirebaseDatabase.getInstance()
    private val userRef = db.getReference("Users")

    // 1. Networking Logic (Moved from Activity)
    override suspend fun initiatePayment(amount: String, userEmail: String, userPhone: String): Result<String> {
        return try {
            val remoteConfig = FirebaseRemoteConfig.getInstance()

            // Wait for config to ensure we have keys
            remoteConfig.fetchAndActivate().await()
            val publicKey = remoteConfig.getString("payment_public_key")
            val secretKey = remoteConfig.getString("payment_secret_key")

            if (publicKey.isEmpty() || secretKey.isEmpty()) {
                return Result.failure(Exception("Payment Gateway Keys missing"))
            }

            val txnId = "TXN-${UUID.randomUUID().toString().take(8)}"

            val formBody = FormBody.Builder()
                .add("public_key", publicKey)
                .add("secret_key", secretKey)
                .add("identifier", txnId)
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
                .add("customer[email]", userEmail)
                .add("customer[mobile]", userPhone)
                .build()

            val request = Request.Builder()
                .url("https://apinepal.com/test/payment/initiate")
                .post(formBody)
                .build()

            // Execute synchronously since we are already in a suspend function (IO context handled by VM)
            val response = client.newCall(request).execute()
            val body = response.body?.string() ?: ""

            if (response.isSuccessful && body.contains("\"success\"")) {
                Result.success(txnId) // Return the Transaction ID on success
            } else {
                Result.failure(Exception("Payment Failed: $body"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // 2. Database Update Logic (The "Subscribe" part)
    override suspend fun activateSubscription(userId: String, transactionId: String): Result<Boolean> {
        return try {
            val startDate = System.currentTimeMillis()
            val calendar = Calendar.getInstance()
            calendar.add(Calendar.DAY_OF_YEAR, 30)
            val expiryDate = calendar.timeInMillis

            val subscription = SubscriptionModel(
                isSubscribed = true,
                startDate = startDate,
                expiryDate = expiryDate,
                lastTransactionId = transactionId
            )

            // 1. Update current subscription status
            userRef.child(userId).child("subscription").setValue(subscription).await()

            // 2. Add to Payment History list for the user
            // This allows the user to see ALL past receipts
            userRef.child(userId).child("paymentHistory").push().setValue(subscription).await()

            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Add this to fetch history
    suspend fun getPaymentHistory(userId: String, callback: (List<SubscriptionModel>) -> Unit) {
        userRef.child(userId).child("paymentHistory").get().addOnSuccessListener { snapshot ->
            val history = snapshot.children.mapNotNull { it.getValue(SubscriptionModel::class.java) }
            callback(history.sortedByDescending { it.startDate })
        }
    }

    // 3. Check Status
    override suspend fun checkSubscription(userId: String, callback: (SubscriptionModel) -> Unit) {
        userRef.child(userId).child("subscription").get().addOnSuccessListener { snapshot ->
            val sub = snapshot.getValue(SubscriptionModel::class.java) ?: SubscriptionModel()

            // Check if expired
            if (sub.isSubscribed && System.currentTimeMillis() > sub.expiryDate) {
                // Auto-expire logic could go here
                callback(sub.copy(isSubscribed = false))
            } else {
                callback(sub)
            }
        }.addOnFailureListener {
            callback(SubscriptionModel())
        }
    }

    override suspend fun getAllTransactions(callback: (List<Pair<String, SubscriptionModel>>) -> Unit) {
        userRef.get().addOnSuccessListener { snapshot ->
            val transactionList = mutableListOf<Pair<String, SubscriptionModel>>()
            for (userSnap in snapshot.children) {
                val fullName = userSnap.child("fullname").getValue(String::class.java) ?: "Unknown"
                val sub = userSnap.child("subscription").getValue(SubscriptionModel::class.java)

                if (sub != null) {
                    // Pair the User's Name with their Subscription details
                    transactionList.add(fullName to sub)
                }
            }
            // Sort by most recent start date
            callback(transactionList.sortedByDescending { it.second.startDate })
        }
    }
}