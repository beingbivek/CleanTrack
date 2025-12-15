package com.example.cleantrack.model

data class LatLngPoint(
    val lat: Double = 0.0,
    val lon: Double = 0.0
)

data class RouteModel(
    val routeId: String = "",
    val name: String = "",
    val points: List<LatLngPoint> = emptyList(),
    val isActive: Boolean = true,
    val createdAt: Long = System.currentTimeMillis()
)
