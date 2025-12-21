package com.example.cleantrack.repository

import com.example.cleantrack.model.PrivacyPolicyModel
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class PrivacyPolicyRepoImpl : PrivacyPolicyRepo {

    private val database: FirebaseDatabase = FirebaseDatabase.getInstance()

    // Recommended node: PrivacyPolicy/policy_1
    private val ref: DatabaseReference = database.getReference("PrivacyPolicy").child("policy_1")

    override fun getPrivacyPolicy(callback: (Boolean, String, PrivacyPolicyModel?) -> Unit) {
        ref.get()
            .addOnSuccessListener { snapshot ->
                if (snapshot.exists()) {
                    val policy = snapshot.getValue(PrivacyPolicyModel::class.java)
                    callback(true, "Privacy policy fetched", policy)
                } else {
                    callback(true, "Privacy policy not set yet", PrivacyPolicyModel())
                }
            }
            .addOnFailureListener { e ->
                callback(false, e.localizedMessage ?: "Failed to fetch privacy policy", null)
            }
    }

    override fun savePrivacyPolicy(model: PrivacyPolicyModel, callback: (Boolean, String) -> Unit) {
        val updated = model.copy(date = System.currentTimeMillis())

        // You can use setValue(updated) also. updateChildren matches your pattern.
        ref.updateChildren(updated.toMap())
            .addOnCompleteListener {
                if (it.isSuccessful) {
                    callback(true, "Privacy policy saved")
                } else {
                    callback(false, it.exception?.localizedMessage ?: "Failed to save privacy policy")
                }
            }
    }
}
