package com.example.cleantrack.model

data class PointsTransactionModel(
    var transactionId: String = "",
    val userId: String = "",
    val amount: Int = 0, // Positive for earnings, negative for redemptions
    val type: String = "", // e.g., "EARNED" or "REDEEMED"
    val description: String = "",
    val timestamp: Long = System.currentTimeMillis()
)