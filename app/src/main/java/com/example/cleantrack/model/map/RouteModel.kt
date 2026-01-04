package com.example.cleantrack.model.map

data class LatLngPoint(
    val lat: Double = 0.0,
    val lon: Double = 0.0
)

data class RouteModel(
    var routeId: String = "",
    val name: String = "",
    val points: List<LatLngPoint> = emptyList(),
    val active: Boolean = true,
    val createdAt: Long = 0L
)
