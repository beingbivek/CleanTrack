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
    var points: Int = 0,
    val userId : String = "",

//    location model updated

    val latitude : Double? = null,
    val longitude : Double? = null,

) : Parcelable{

    @Exclude
    fun toMap() : Map<String, Any?>{
        return mapOf(

            "email" to email,
            "fullname" to fullname,
            "number" to number,
            "role" to role,
            "isSubscribed" to isSubscribed,
            "points" to points,
            "userId" to userId,
            "latitude" to latitude,
            "longitude" to longitude
        )

    }
}
