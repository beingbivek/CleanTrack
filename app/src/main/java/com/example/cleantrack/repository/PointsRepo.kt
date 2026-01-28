package com.example.cleantrack.repository

import com.example.cleantrack.model.LeaderboardModel
import com.example.cleantrack.model.PointsTransactionModel

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

    fun getLeaderboardData(callback: (List<LeaderboardModel>) -> Unit)

    fun saveTransaction(transaction: PointsTransactionModel, callback: (Boolean) -> Unit)

    fun getPointsHistory(userId: String, callback: (List<PointsTransactionModel>) -> Unit)

}
