package com.example.cleantrack.repository

import com.example.cleantrack.model.RouteInsightModel

interface RouteInsightRepo {
    fun saveInsight(insight: RouteInsightModel, callback: (Boolean) -> Unit)
    fun getAllInsights(callback: (Boolean, List<RouteInsightModel>?) -> Unit)

    fun getInsightsByUserId(userId: String, callback: (Boolean, List<RouteInsightModel>?) -> Unit)
}