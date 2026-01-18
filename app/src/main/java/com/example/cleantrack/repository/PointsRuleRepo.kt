package com.example.cleantrack.repository

import com.example.cleantrack.model.PointsRuleModel

interface PointsRuleRepo {

    fun addRule(
        model: PointsRuleModel,
        callback: (Boolean, String) -> Unit
    )

    fun getAllRules(
        callback: (Boolean, String, List<PointsRuleModel>?) -> Unit
    )

    fun updateRule(
        model: PointsRuleModel,
        callback: (Boolean, String) -> Unit
    )

    fun deleteRule(
        ruleId: String,
        callback: (Boolean, String) -> Unit
    )
}
