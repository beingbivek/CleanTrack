package com.example.cleantrack.util

import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import kotlinx.coroutines.tasks.await

object ApiTokenUtil {
    private const val BAATO_API_KEY_PARAM = "baato_api_key"
    private const val GEMINI_API_KEY_PARAM = "gemini_api_key"

    suspend fun getBaatoApiKey(): String? {
        return try {
            val remoteConfig = FirebaseRemoteConfig.getInstance()
            remoteConfig.fetchAndActivate().await()
            remoteConfig.getString(BAATO_API_KEY_PARAM).takeIf { it.isNotBlank() }
        } catch (_: Exception) {
            null
        }
    }

    suspend fun getGeminiApiKey(): String? {
        return try {
            val remoteConfig = FirebaseRemoteConfig.getInstance()
            remoteConfig.fetchAndActivate().await()
            remoteConfig.getString(GEMINI_API_KEY_PARAM).takeIf { it.isNotBlank() }
        } catch (_: Exception) {
            null
        }
    }
}
