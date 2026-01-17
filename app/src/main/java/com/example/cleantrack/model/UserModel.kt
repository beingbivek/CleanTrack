package com.example.cleantrack.model

import android.os.Parcelable
import com.google.firebase.database.Exclude
import kotlinx.parcelize.Parcelize
import kotlinx.parcelize.RawValue


@Parcelize
data class UserModel(
    val email : String = "",
    val fullname : String = "",
    val number : String = "",
    val role : String = "USER",
    var subscribed: Boolean? = null,
    var stability: Int? = null,
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
    val subscription: @RawValue SubscriptionModel? = null,

    ) : Parcelable{

    @Exclude
    fun toMap() : Map<String, Any?>{
        val data = mutableMapOf<String, Any?>(
            "email" to email,
            "fullname" to fullname,
            "number" to number,
            "role" to role,
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

        subscribed?.let { data["subscribed"] = it }
        stability?.let { data["stability"] = it }
        subscription?.let { data["subscription"] = it }

        return data
    }
}
