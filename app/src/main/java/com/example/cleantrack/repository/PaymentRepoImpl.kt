package com.example.cleantrack.repository

import com.example.cleantrack.model.PaymentInitiationModel
import com.example.cleantrack.model.SubscriptionModel
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import kotlinx.coroutines.tasks.await
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.FormBody
import org.json.JSONObject
import java.util.Calendar
import java.util.UUID

class PaymentRepoImpl : PaymentRepo {

    private val client = OkHttpClient()
    private val db = FirebaseDatabase.getInstance()
    private val userRef = db.getReference("Users")
    private val subscriptionConfigRef = db.getReference("SubscriptionConfig")

    // 1. Networking Logic (Moved from Activity)
    // In PaymentRepoImpl.kt
    override suspend fun initiatePayment(amount: String, userEmail: String, userPhone: String): Result<PaymentInitiationModel> {
        return try {
            val remoteConfig = FirebaseRemoteConfig.getInstance()
            remoteConfig.fetchAndActivate().await()
            val secretKey = remoteConfig.getString("stripe_secret_key")
                .trim()
            if (secretKey.isBlank()) {
                return Result.failure(IllegalStateException("Payment secret key is missing."))
            }
            val authHeader = if (secretKey.startsWith("Bearer ")) secretKey else "Bearer $secretKey"
            val amountInMinorUnit = (amount.toDouble() * 100).toInt()

            val requestBody = FormBody.Builder()
                .add("amount", amountInMinorUnit.toString())
                .add("currency", "inr")
                .add("automatic_payment_methods[enabled]", "true")
                .add("description", "CleanTrack Subscription")
                .add("metadata[user_email]", userEmail)
                .add("metadata[user_phone]", userPhone)
                .add("metadata[order_id]", UUID.randomUUID().toString().take(8))
                .build()

            val request = Request.Builder()
                .url("https://api.stripe.com/v1/payment_intents")
                .header("Authorization", authHeader)
                .post(requestBody)
                .build()

            val response = client.newCall(request).execute()
            val responseBody = response.body?.string().orEmpty()

            val jsonResponse = runCatching { JSONObject(responseBody) }.getOrNull()
            if (!response.isSuccessful) {
                val message = jsonResponse?.optJSONObject("error")?.optString("message")
                    ?.takeIf { it.isNotBlank() }
                    ?: response.message.ifBlank { "Failed to initiate payment." }
                return Result.failure(IllegalStateException(message))
            }

            val paymentIntentId = jsonResponse?.optString("id").orEmpty()
            val clientSecret = jsonResponse?.optString("client_secret").orEmpty()
            if (paymentIntentId.isBlank() || clientSecret.isBlank()) {
                val message = "Payment initiation response missing required fields."
                return Result.failure(IllegalStateException(message))
            }
            Result.success(PaymentInitiationModel(paymentIntentId = paymentIntentId, clientSecret = clientSecret))
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

    override suspend fun getSubscriptionAmount(): Result<String> {
        return try {
            val snapshot = subscriptionConfigRef.child("monthlyAmount").get().await()
            val amount = snapshot.getValue(String::class.java)?.trim().orEmpty()
            Result.success(amount.ifBlank { "500" })
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateSubscriptionAmount(amount: String): Result<Boolean> {
        return try {
            subscriptionConfigRef.child("monthlyAmount").setValue(amount.trim()).await()
            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
