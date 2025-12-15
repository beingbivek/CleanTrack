package com.example.cleantrack.repository

import com.example.cleantrack.model.PrivacyPolicyModel
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ktx.getValue
import kotlinx.coroutines.tasks.await

class PrivacyPolicyRepoImpl : PrivacyPolicyRepo {

    private val db = FirebaseDatabase.getInstance()
    private val ref = db.getReference("privacy_policy")

    override suspend fun getPrivacyPolicy(): PrivacyPolicyModel? {
        return try {
            // Option A: store as a SINGLE object at /privacy_policy
            val snapshot = ref.get().await()
            snapshot.getValue<PrivacyPolicyModel>()
        } catch (e: Exception) {
            null
        }
    }
}
