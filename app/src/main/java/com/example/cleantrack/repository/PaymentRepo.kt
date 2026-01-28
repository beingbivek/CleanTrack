package com.example.cleantrack.repository

import com.example.cleantrack.model.PaymentInitiationModel
import com.example.cleantrack.model.SubscriptionModel

interface PaymentRepo {
    suspend fun initiatePayment(amount: String, userEmail: String, userPhone: String): Result<PaymentInitiationModel>
    suspend fun activateSubscription(userId: String, transactionId: String): Result<Boolean>
    suspend fun checkSubscription(userId: String, callback: (SubscriptionModel) -> Unit)
    suspend fun getAllTransactions(callback: (List<Pair<String, SubscriptionModel>>) -> Unit)
    suspend fun getSubscriptionAmount(): Result<String>
    suspend fun updateSubscriptionAmount(amount: String): Result<Boolean>
}
