package com.example.cleantrack.repository

import com.example.cleantrack.model.LeaderBoardUser

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

    fun getLeaderboardData(callback: (List<LeaderBoardUser>) -> Unit)

}
