package com.example.cleantrack.repository

import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class TermsAndConditionRepoImpl : TermsAndConditionRepo {
    private val db = FirebaseDatabase.getInstance().getReference("TermsAndCondition")

    override fun updateTermsAndCondition(content: String, callback: (Boolean, String) -> Unit) {
        db.child("current").setValue(content)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) callback(true, "Terms & Conditions updated!")
                else callback(false, task.exception?.message ?: "Update failed")
            }
    }

    override fun getTermsAndCondition(callback: (Boolean, String, String?) -> Unit) {
        db.child("current").addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val content = snapshot.getValue(String::class.java)
                callback(true, "Loaded", content)
            }
            override fun onCancelled(error: DatabaseError) {
                callback(false, error.message, null)
            }
        })
    }
}