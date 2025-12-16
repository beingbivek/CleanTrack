package com.example.cleantrack.model

data class UserModel(
    val email : String = "",
    val fullname : String = "",
    val number : String = "",
    val role : String = "USER",
    val userId : String = "",

//    location model updated

    val latitude : Double? = null,
    val longitude : Double? = null,

){
    fun toMap() : Map<String, Any?>{
        return mapOf(

          "email" to email,
            "fullname" to fullname,
            "number" to number,
            "role" to role,
            "userId" to userId,
            "latitude" to latitude,
            "longitude" to longitude
        )

    }
}
