package com.example.cleantrack.repository

import com.example.cleantrack.model.PointsRuleModel
import com.google.firebase.database.*

class PointsRuleRepoImpl : PointsRuleRepo {

    private val ref: DatabaseReference =
        FirebaseDatabase.getInstance().getReference("PointsRules")

    override fun addRule(
        model: PointsRuleModel,
        callback: (Boolean, String) -> Unit
    ) {
        model.ruleId = ref.push().key ?: ""
        ref.child(model.ruleId)
            .setValue(model)
            .addOnCompleteListener {
                if (it.isSuccessful) {
                    callback(true, "Rule added")
                } else {
                    callback(false, it.exception?.message ?: "Error")
                }
            }
    }

    override fun getAllRules(
        callback: (Boolean, String, List<PointsRuleModel>?) -> Unit
    ) {
        ref.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val list = mutableListOf<PointsRuleModel>()
                for (child in snapshot.children) {
                    val rule = child.getValue(PointsRuleModel::class.java)
                    if (rule != null) list.add(rule)
                }
                callback(true, "Fetched", list)
            }

            override fun onCancelled(error: DatabaseError) {
                callback(false, error.message, null)
            }
        })
    }

    override fun updateRule(
        model: PointsRuleModel,
        callback: (Boolean, String) -> Unit
    ) {
        ref.child(model.ruleId)
            .updateChildren(model.toMap())
            .addOnCompleteListener {
                if (it.isSuccessful) {
                    callback(true, "Rule updated")
                } else {
                    callback(false, it.exception?.message ?: "Error")
                }
            }
    }

    override fun deleteRule(
        ruleId: String,
        callback: (Boolean, String) -> Unit
    ) {
        ref.child(ruleId)
            .removeValue()
            .addOnCompleteListener {
                if (it.isSuccessful) {
                    callback(true, "Rule deleted")
                } else {
                    callback(false, it.exception?.message ?: "Error")
                }
            }
    }
}
