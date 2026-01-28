package com.example.cleantrack.repository

import com.example.cleantrack.model.LeaderboardModel
import com.example.cleantrack.model.PointsRuleModel
import com.example.cleantrack.model.PointsTransactionModel
import com.example.cleantrack.model.UserModel
import com.example.cleantrack.model.UserPointsModel
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.MutableData
import com.google.firebase.database.Transaction
import com.google.firebase.database.ValueEventListener

class PointsRepoImpl : PointsRepo {

    private val db = FirebaseDatabase.getInstance()
    private val rulesRef = db.getReference("PointsRules")
    private val userPointsRef = db.getReference("UserPoints")
    private val historyRef = db.getReference("PointsHistory")

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

    override fun getLeaderboardData(callback: (List<LeaderboardModel>) -> Unit) {
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
                val leaderboardList = mutableListOf<LeaderboardModel>()
                usersSnapshot.children.forEach { child ->
                    val userModel = child.getValue(UserModel::class.java)
                    if (userModel != null) {
                        leaderboardList.add(
                            LeaderboardModel(
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

                val newTotal = current + points

                userPointsRef.child(userId).setValue(
                    UserPointsModel(
                        userId = userId,
                        totalPoints = newTotal,
                        lastUpdated = System.currentTimeMillis()
                    )
                ).addOnSuccessListener {
                    // Log the transaction in history node automatically
                    val transaction = PointsTransactionModel(
                        userId = userId,
                        amount = points,
                        type = "EARNED",
                        description = "Points earned from disposal"
                    )
                    saveTransaction(transaction) { /* Optional: handle log success */ }
                }
            }
    }

    override fun saveTransaction(transaction: PointsTransactionModel, callback: (Boolean) -> Unit) {
        val key = historyRef.push().key ?: ""
        transaction.transactionId = key

        historyRef.child(key).setValue(transaction)
            .addOnCompleteListener {
                callback(it.isSuccessful)
            }
    }

    override fun getPointsHistory(userId: String, callback: (List<PointsTransactionModel>) -> Unit) {
        historyRef.orderByChild("userId").equalTo(userId)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val historyList = snapshot.children.mapNotNull {
                        it.getValue(PointsTransactionModel::class.java)
                    }
                    // Sort by newest timestamp first
                    callback(historyList.sortedByDescending { it.timestamp })
                }

                override fun onCancelled(error: DatabaseError) {
                    callback(emptyList())
                }
            })
    }

    override fun deductPoints(userId: String, amount: Int, description: String, callback: (Boolean, String) -> Unit) {
        val ref = userPointsRef.child(userId)
        ref.runTransaction(object : Transaction.Handler {
            override fun doTransaction(mutableData: MutableData): Transaction.Result {
                val model = mutableData.getValue(UserPointsModel::class.java)
                if (model == null || model.totalPoints < amount) return Transaction.abort()

                model.totalPoints -= amount
                mutableData.value = model
                return Transaction.success(mutableData)
            }

            override fun onComplete(error: DatabaseError?, committed: Boolean, snapshot: DataSnapshot?) {
                if (committed) {
                    // Log the deduction in Points History for transparency
                    val txn = PointsTransactionModel(
                        userId = userId,
                        amount = -amount,
                        type = "REDEEMED",
                        description = description
                    )
                    saveTransaction(txn) { callback(true, "Points deducted") }
                } else {
                    callback(false, error?.message ?: "Insufficient points")
                }
            }
        })
    }


}
