package com.example.cleantrack.model

data class UserPointsModel(
    val userId: String = "",
    var totalPoints: Int = 0,
    val lastUpdated: Long = System.currentTimeMillis()
)
