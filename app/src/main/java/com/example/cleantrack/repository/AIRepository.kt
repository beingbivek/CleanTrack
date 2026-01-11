package com.example.cleantrack.repository
import android.util.Log
import com.google.ai.client.generativeai.GenerativeModel

class AIRepository {
    // Note: In a real app, use a Backend/Cloud Function to hide your API Key
    private val generativeModel = GenerativeModel(
        modelName = "gemini-2.5-flash",
        apiKey = "AIzaSyDkShUJAephmpoqWHaP1HV5PWIOJY-O0s8"
    )

    suspend fun generateImprovementTip(rating: Int, remarks: String, segregatedCorrectly: Boolean): String {

        val segregationStatus = if (segregatedCorrectly) "excellent" else "poor"

        val prompt = """
            User Waste Collection Report:
            - Star Rating: $rating/5
            - Segregation Quality: $segregationStatus
            - Driver's Note: "$remarks"
            
            Action: Write a friendly, 1-sentence coaching tip for the user. 
            If segregation was poor, suggest how to fix it based on the remarks. 
            If it was excellent, give a brief praise. Keep it under 15 words.
        """.trimIndent()

        return try {
            val response = generativeModel.generateContent(prompt)
            response.text ?: "Thanks for disposing of your waste! Keep it up."
        } catch (e: Exception) {
            Log.e("AI_ERROR", "Reason: ${e.message}") // Look for "AI_ERROR" in Logcat
            "AI Error: ${e.localizedMessage}"
        }
    }
}