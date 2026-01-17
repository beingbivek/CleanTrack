package com.example.cleantrack.repository
import android.util.Log
import com.example.cleantrack.model.BinCollectionModel
import com.google.ai.client.generativeai.GenerativeModel

class AIRepository {
    private val generativeModel = GenerativeModel(
        modelName = "gemini-2.5-flash", // Using stable flash for faster summaries
        apiKey = "AIzaSyCgwxy0WubzG-vpffwAZGyBegugFuA3dIo"

    )

    suspend fun generateGlobalOverview(history: List<BinCollectionModel>): String {
        if (history.isEmpty()) return "Start your collection journey to see AI insights!"

        // Create a summary string of all bin data for the AI
        val summaryData = history.joinToString("\n") {
            "- Rating: ${it.rating}/5, Segregation: ${if(it.segregatedCorrectly) "Success" else "Failed"}, Driver Note: ${it.remarks}"
        }

        val prompt = """
            You are an expert waste management consultant. Review the following historical data for a user's waste collection:
            $summaryData
            
            Based on ALL these records:
            1. Identify if there is a recurring problem (e.g., consistently bad segregation).
            2. Provide one single, high-impact piece of advice or praise.
            3. Keep the response friendly and under 25 words.
            
            Give a harsh yet meaningful review, if the user hasn't segregated tell the user which bin was bad and how the user can improve it.
        """.trimIndent()

        return try {
            val response = generativeModel.generateContent(prompt)
            response.text ?: "You're doing a great job maintaining your waste habits!"
        } catch (e: Exception) {
            Log.e("AI_ERROR", "Reason: ${e.message}") // Look for "AI_ERROR" in Logcat
            "AI Error: ${e.localizedMessage}"
        }
    }
}