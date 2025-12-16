package com.example.cleantrack.model

data class PrivacyPolicyModel(
    val privacypolicyId: String = "policy_1",
    val description: String = "",
    val date: Long = 0L
) {
    fun toMap(): Map<String, Any?> {
        return mapOf(
            "privacypolicyId" to privacypolicyId,
            "description" to description,
            "date" to date
        )
    }
}
