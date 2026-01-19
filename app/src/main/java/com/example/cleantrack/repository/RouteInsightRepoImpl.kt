package com.example.cleantrack.repository

import com.example.cleantrack.model.RouteInsightModel
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class RouteInsightRepoImpl : RouteInsightRepo {
    private val ref = FirebaseDatabase.getInstance().getReference("RouteInsights")

    override fun saveInsight(insight: RouteInsightModel, callback: (Boolean) -> Unit) {
        val id = ref.push().key ?: return
        insight.insightId = id
        ref.child(id).setValue(insight).addOnCompleteListener { callback(it.isSuccessful) }
    }

    override fun getAllInsights(callback: (Boolean, List<RouteInsightModel>?) -> Unit) {
        ref.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val list = snapshot.children.mapNotNull { it.getValue(RouteInsightModel::class.java) }
                callback(true, list)
            }
            override fun onCancelled(error: DatabaseError) = callback(false, null)
        })
    }
}