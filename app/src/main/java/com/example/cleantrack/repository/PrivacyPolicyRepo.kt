package com.example.cleantrack.repository

import com.example.cleantrack.model.PrivacyPolicyModel

interface PrivacyPolicyRepo {
    suspend fun getPrivacyPolicy(): PrivacyPolicyModel?
}