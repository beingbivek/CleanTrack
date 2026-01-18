package com.example.cleantrack.repository

import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.example.cleantrack.util.NotificationTypes

class PrivacyPolicyRepoImpl : PrivacyPolicyRepo {
    private val db = FirebaseDatabase.getInstance().getReference("PrivacyPolicy")
    private val notificationRepo = NotificationRepoImpl()

    override fun updatePrivacyPolicy(content: String, callback: (Boolean, String) -> Unit) {
        db.child("current").setValue(content)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    notificationRepo.sendToRole(
                        "USER",
                        "Privacy policy updated",
                        "Please review the latest privacy policy.",
                        NotificationTypes.POLICY_UPDATED
                    )
                    notificationRepo.sendToRole(
                        "DRIVER",
                        "Privacy policy updated",
                        "Please review the latest privacy policy.",
                        NotificationTypes.POLICY_UPDATED
                    )
                    notificationRepo.sendToRole(
                        "ADMIN",
                        "Privacy policy updated",
                        "Privacy policy changes were published.",
                        NotificationTypes.POLICY_UPDATED
                    )
                    callback(true, "Privacy Policy updated!")
                } else {
                    callback(false, task.exception?.message ?: "Update failed")
                }
            }
    }

    override fun getPrivacyPolicy(callback: (Boolean, String, String?) -> Unit) {
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
