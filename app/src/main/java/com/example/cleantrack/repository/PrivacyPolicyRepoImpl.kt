package com.example.cleantrack.repository

import com.example.cleantrack.model.PrivacyPolicyModel
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class PrivacyPolicyRepoImpl :  PrivacyPolicyRepo {

    private val db = FirebaseFirestore.getInstance()

    override suspend fun getPrivacyPolicy(): PrivacyPolicyModel? {
        return try {
            val snapshot = db.collection("privacy_policy").limit(1).get().await()
            if (!snapshot.isEmpty) {
                snapshot.documents[0].toObject(PrivacyPolicyModel::class.java)
            } else null
        } catch (e: Exception) {
            null
        }
    }
}
