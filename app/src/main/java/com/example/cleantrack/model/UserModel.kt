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
    val userId : String = "",

    // Added Address Fields
    val province: String = "",
    val district: String = "",
    val municipality: String = "",
    val ward: String = "",

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
            "userId" to userId,
            "province" to province,
            "district" to district,
            "municipality" to municipality,
            "ward" to ward,
            "latitude" to latitude,
            "longitude" to longitude,
            "profileImageUrl" to profileImageUrl
        )

    }
}
