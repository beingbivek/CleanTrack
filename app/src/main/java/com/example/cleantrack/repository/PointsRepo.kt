package com.example.cleantrack.repository

interface PointsRepo {

    fun calculatePoints(
        binType: String,
        segregatedCorrectly: Boolean,
        callback: (Int) -> Unit
    )

    fun addPointsToUser(
        userId: String,
        points: Int
    )
}
