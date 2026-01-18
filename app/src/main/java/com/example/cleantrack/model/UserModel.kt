package com.example.cleantrack.model

import android.os.Parcelable
import com.google.firebase.database.Exclude
import kotlinx.parcelize.Parcelize



@Parcelize
data class UserModel(
    val email : String = "",
    val fullname : String = "",
    val number : String = "",
    val role : String = "USER",
    val isSubscribed: Boolean = false,
    val subscription: SubscriptionModel? = null,
    val expiryDate: Long? = null,
    var points: Int = 0,
    val userId : String = "",

    // Added Address Fields
    val province: String = "",
    val district: String = "",
    val municipality: String = "",
    val ward: String = "",

    val activeRouteId: String = "",

//    location model updated

    val latitude : Double? = null,
    val longitude : Double? = null,

    val profileImageUrl: String = "",

) : Parcelable{

    @Exclude
    fun toMap() : Map<String, Any?>{
        return mapOf(

            "email" to email,
            "fullname" to fullname,
            "number" to number,
            "role" to role,
            "isSubscribed" to isSubscribed,
            "subscription" to subscription,
            "expiryDate" to expiryDate,

            "points" to points,
            "userId" to userId,
            "province" to province,
            "district" to district,
            "municipality" to municipality,
            "ward" to ward,
            "activeRouteId" to activeRouteId,
            "latitude" to latitude,
            "longitude" to longitude,
            "profileImageUrl" to profileImageUrl
        )

    }
}
