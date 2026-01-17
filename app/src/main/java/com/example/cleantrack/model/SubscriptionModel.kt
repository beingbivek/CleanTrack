package com.example.cleantrack.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize // Add this
data class SubscriptionModel(
    val isSubscribed: Boolean = false,
    val startDate: Long = 0,
    val expiryDate: Long = 0,
    val planType: String = "Monthly",
    val lastTransactionId: String = ""
) : Parcelable // Add this