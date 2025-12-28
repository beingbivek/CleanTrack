package com.example.cleantrack.model

data class PointsRuleModel(
    var ruleId: String = "",
    val binType: String = "ORGANIC",
    val segregatedCorrectly: Boolean = true,
    val points: Int = 0,
    val isActive: Boolean = true
) {
    fun toMap(): Map<String, Any?> {
        return mapOf(
            "ruleId" to ruleId,
            "binType" to binType,
            "segregatedCorrectly" to segregatedCorrectly,
            "points" to points,
            "isActive" to isActive
        )
    }
}
