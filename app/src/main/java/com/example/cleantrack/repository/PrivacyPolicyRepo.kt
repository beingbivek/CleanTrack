package com.example.cleantrack.repository

import com.example.cleantrack.model.PrivacyPolicyModel

interface PrivacyPolicyRepo {

    fun getPrivacyPolicy(callback: (Boolean, String, PrivacyPolicyModel?) -> Unit)

    fun savePrivacyPolicy(model: PrivacyPolicyModel, callback: (Boolean, String) -> Unit)
}
