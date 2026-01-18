package com.example.cleantrack.repository


interface PrivacyPolicyRepo {
    fun updatePrivacyPolicy(content: String, callback: (Boolean, String) -> Unit)
    fun getPrivacyPolicy(callback: (Boolean, String, String?) -> Unit)
}