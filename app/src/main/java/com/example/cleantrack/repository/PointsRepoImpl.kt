package com.example.cleantrack.repository

import com.example.cleantrack.model.LeaderBoardUser
import com.example.cleantrack.model.PointsRuleModel
import com.example.cleantrack.model.UserModel
import com.example.cleantrack.model.UserPointsModel
import com.google.firebase.database.FirebaseDatabase

class PointsRepoImpl : PointsRepo {

    private val db = FirebaseDatabase.getInstance()
    private val rulesRef = db.getReference("PointsRules")
    private val userPointsRef = db.getReference("UserPoints")

    override fun calculatePoints(
        binType: String,
        segregatedCorrectly: Boolean,
        callback: (Int) -> Unit
    ) {
        rulesRef
            .orderByChild("binType")
            .equalTo(binType)
            .get()
            .addOnSuccessListener { snapshot ->
                if (!snapshot.exists()) {
                    callback(0) // No rules found for this type
                    return@addOnSuccessListener
                }

                for (child in snapshot.children) {
                    val rule = child.getValue(PointsRuleModel::class.java)
                    if (rule != null &&
                        rule.isActive &&
                        rule.segregatedCorrectly == segregatedCorrectly
                    ) {
                        callback(rule.points)
                        return@addOnSuccessListener
                    }
                }
                callback(0) // Rules found but none matched segregation status
            }
            .addOnFailureListener {
                // CRITICAL FIX: If Firebase fails, return 0 so the UI can finish
                callback(0)
            }
    }

    override fun getLeaderboardData(callback: (List<LeaderBoardUser>) -> Unit) {
        val usersRef = db.getReference("Users") // Assuming your UserModel is here
        val userPointsRef = db.getReference("UserPoints")

        // 1. Fetch all points
        userPointsRef.get().addOnSuccessListener { pointsSnapshot ->
            val pointsMap = mutableMapOf<String, Int>()
            pointsSnapshot.children.forEach { child ->
                val model = child.getValue(UserPointsModel::class.java)
                if (model != null) pointsMap[model.userId] = model.totalPoints
            }

            // 2. Fetch user details to get names and images
            usersRef.get().addOnSuccessListener { usersSnapshot ->
                val leaderboardList = mutableListOf<LeaderBoardUser>()
                usersSnapshot.children.forEach { child ->
                    val userModel = child.getValue(UserModel::class.java)
                    if (userModel != null) {
                        leaderboardList.add(
                            LeaderBoardUser(
                                userId = userModel.userId,
                                fullname = userModel.fullname,
                                points = pointsMap[userModel.userId] ?: 0,
                                profileImageUrl = userModel.profileImageUrl
                            )
                        )
                    }
                }
                // 3. Sort by points descending
                callback(leaderboardList.sortedByDescending { it.points })
            }
        }
    }

    override fun addPointsToUser(userId: String, points: Int) {
        userPointsRef.child(userId).get()
            .addOnSuccessListener { snapshot ->
                val current = snapshot
                    .child("totalPoints")
                    .getValue(Int::class.java) ?: 0

                userPointsRef.child(userId).setValue(
                    UserPointsModel(
                        userId = userId,
                        totalPoints = current + points,
                        lastUpdated = System.currentTimeMillis()
                    )
                )
            }
    }


}
