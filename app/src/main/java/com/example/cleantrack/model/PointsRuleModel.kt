package com.example.cleantrack.model

data class PointsRuleModel(
    val ruleId: String = "",
    val binType: String = "ORGANIC",
    val segregatedCorrectly: Boolean = true,
    val points: Int = 10,
    val isActive: Boolean = true
)
