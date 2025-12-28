package com.example.cleantrack.model

data class UserPointsModel(
    val userId: String = "",
    val totalPoints: Int = 0,
    val lastUpdated: Long = System.currentTimeMillis()
)
