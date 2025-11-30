package com.example.cleantrack.model

data class UserModel(
    val email : String,
    val fullname : String,
    val number : String,
    val role : String = "USER",
    val userId : String,
)
