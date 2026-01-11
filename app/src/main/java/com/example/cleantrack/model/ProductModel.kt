package com.example.cleantrack.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class ProductModel(
    val id: String = "",
    val name: String = "",
    val description: String = "",
    val imageUrl: String = "",
    val category: String = "",
    val startingBidPrice: Double = 0.0,
    val currentBidPrice: Double = 0.0,
    val status: String = "active", // e.g., "active", "sold", "ended"
    val auctionEndTime: Long = 0L, // Timestamp for auction end
    val sellerId: String = "",
    val sellerPhoneNumber: String = "", // Used after acceptance
    val highestBidderId: String = "",
    val highestBidderPhoneNumber: String = "", // Used after acceptance
    val bids: Map<String, Double> = emptyMap() // userId to bidAmount
) : Parcelable