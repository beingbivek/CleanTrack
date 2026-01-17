package com.example.cleantrack.repository

import com.example.cleantrack.model.SubscriptionModel

interface PaymentRepo {
    suspend fun initiatePayment(amount: String, userEmail: String, userPhone: String): Result<String>
    suspend fun activateSubscription(userId: String, transactionId: String): Result<Boolean>
    suspend fun checkSubscription(userId: String, callback: (SubscriptionModel) -> Unit)
    suspend fun getAllTransactions(callback: (List<Pair<String, SubscriptionModel>>) -> Unit)
}