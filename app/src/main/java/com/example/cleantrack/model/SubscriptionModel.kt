package com.example.cleantrack.model

data class SubscriptionModel(
    val isSubscribed: Boolean = false,
    val startDate: Long = 0,
    val expiryDate: Long = 0,
    val planType: String = "Monthly",
    val lastTransactionId: String = ""
)