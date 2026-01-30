package com.example.cleantrack.repository
import android.util.Log
import com.example.cleantrack.model.BinCollectionModel
import com.example.cleantrack.model.RouteInsightModel
import com.example.cleantrack.model.map.RouteModel
import com.example.cleantrack.util.ApiTokenUtil
import com.google.ai.client.generativeai.GenerativeModel
import com.google.firebase.database.FirebaseDatabase

class AIRepository {
    private suspend fun buildGenerativeModel(): GenerativeModel? {
        val apiKey = ApiTokenUtil.getGeminiApiKey()
        if (apiKey.isNullOrBlank()) {
            Log.e("AI_ERROR", "Gemini API key missing from Remote Config")
            return null
        }
        return GenerativeModel(
            modelName = "gemini-2.5-flash", // Using stable flash for faster summaries
            apiKey = apiKey
        )
    }

    suspend fun generateGlobalOverview(history: List<BinCollectionModel>): String {
        if (history.isEmpty()) return "Start your collection journey to see AI insights!"

        val generativeModel = buildGenerativeModel()
            ?: return "AI Error: Gemini API key is not configured."

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
            
            Give a meaningful review, if the user hasn't segregated tell the user which bin was bad and how the user can improve it.
            
            If the user is improving their waste management praise them and tell them to continue managing in same way.
        """.trimIndent()

        return try {
            val response = generativeModel.generateContent(prompt)
            response.text ?: "You're doing a great job maintaining your waste habits!"
        } catch (e: Exception) {
            Log.e("AI_ERROR", "Reason: ${e.message}") // Look for "AI_ERROR" in Logcat
            "AI Error: ${e.localizedMessage}"
        }
    }


    suspend fun generateStrategyFromInsights(insights: List<RouteInsightModel>): String {
        if (insights.isEmpty()) return "No historical insights available to analyze."

        val generativeModel = buildGenerativeModel() ?: return "AI Configuration Error"

        // Grouping insights to show the AI a condensed history
        val narrativeData = insights.groupBy { it.routeName }.entries.joinToString("\n") { (name, logs) ->
            "Route: $name | Bad Segregations: ${logs.count { !it.segregated }} | Avg Rating: ${logs.map { it.rating }.average()}"
        }

        val prompt = """
        You are a Waste Management Operations Strategist.
        Review the following summarized performance data for city routes:
        $narrativeData
        
        Task:
        1. Identify the 'Critical Route' (Worst) and the 'Model Route' (Best).
        2. Provide a specific operational directive for the Admin.
        3. Use professional, concise language.
        4. Keep it under 60 words.
    """.trimIndent()

        return try {
            val response = generativeModel.generateContent(prompt)
            response.text ?: "System operating normally."
        } catch (e: Exception) {
            "Strategic Analysis Error: ${e.localizedMessage}"
        }
    }

    // This logic should run after the AI generates a review for the user
    // Inside AIRepository.kt
    fun saveProcessedInsight(collection: BinCollectionModel, aiReview: String, routeId: String, routeName: String, ownerId: String) {
        val insightRef = FirebaseDatabase.getInstance().getReference("RouteInsights")
        val id = insightRef.push().key ?: ""

        val newInsight = RouteInsightModel(
            insightId = id,
            userId = ownerId,
            tripId = collection.tripId,
            routeId = routeId,
            routeName = routeName,
            aiResponse = aiReview,
            rating = collection.rating,
            segregated = collection.segregatedCorrectly,
            timestamp = System.currentTimeMillis()
        )

        insightRef.child(id).setValue(newInsight)
    }
}