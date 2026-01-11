package com.example.cleantrack.model



import android.os.Parcelable
import androidx.annotation.Keep
import com.google.firebase.database.Exclude
import kotlinx.android.parcel.Parcelize

@Keep
@Parcelize
data class ProductModel(
    var productId: String = "",
    val productName: String = "",
    val pDescription: String = "",
    val pImageUrl: String = "",
    val pCategory: String = "",
    val startingBidPrice: Double = 0.0,
    val currentBidPrice: Double = 0.0,
    val productStatus: String = "active",
    val auctionEndTime: Long = 0L,
    val sellerId: String = "",
    val sellerPhoneNumber: String = "",
    val highestBidderId: String = "",
    val highestBidderPhoneNumber: String = "",
    // This represents the "bidders" folder where key = userId, value = amount
    val bids: Map<String, Double>? = null
) : Parcelable {

    @Exclude
    fun toMap(): Map<String, Any?> {
        return mapOf(
            "productId" to productId,
            "productName" to productName,
            "pDescription" to pDescription,
            "pImageUrl" to pImageUrl,
            "pCategory" to pCategory,
            "startingBidPrice" to startingBidPrice,
            "currentBidPrice" to currentBidPrice,
            "productStatus" to productStatus,
            "auctionEndTime" to auctionEndTime,
            "sellerId" to sellerId,
            "sellerPhoneNumber" to sellerPhoneNumber,
            "highestBidderId" to highestBidderId,
            "highestBidderPhoneNumber" to highestBidderPhoneNumber,
            "bids" to bids // Firebase will render this as a child node with multiple bidder IDs
        )
    }
}