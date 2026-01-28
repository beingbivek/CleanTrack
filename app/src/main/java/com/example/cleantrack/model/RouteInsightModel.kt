package com.example.cleantrack.model

data class RouteInsightModel(
    var insightId: String = "",
    val tripId: String = "",
    val routeId: String = "",
    val routeName: String = "",
    val aiResponse: String = "",
    val rating: Int = 0,
    val segregated: Boolean = false, // Must be here for logs.filter { !it.segregated }
    val timestamp: Long = System.currentTimeMillis()
)