package com.example.cleantrack.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class ProductModel(
    var productId: String = "",
    val productName: String = "",
    val pDescription: String = "",
    val pImageUrl: String = "",
    val pCategory: String = "",
    val startingBidPrice: Double = 0.0,
    val currentBidPrice: Double = 0.0,
    val productStatus: String = "active", // e.g., "active", "sold", "ended"
    val auctionEndTime: Long = 0L, // Timestamp for auction end
    val sellerId: String = "",
    val sellerPhoneNumber: String = "", // Used after acceptance
    val highestBidderId: String = "",
    val highestBidderPhoneNumber: String = "", // Used after acceptance
    val bids: Map<String, Double> = emptyMap() // userId to bidAmount
) : Parcelable