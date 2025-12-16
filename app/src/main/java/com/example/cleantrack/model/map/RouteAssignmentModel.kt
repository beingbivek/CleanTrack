package com.example.cleantrack.model.map

data class RouteAssignmentModel(
    val assignmentId: String = "",
    val routeId: String = "",
    val routeName: String = "",
    val driverId: String = "",
    val driverName: String = "",
    val daysOfWeek: List<Int> = emptyList(),
    val startTime: String = "06:00",
    val endTime: String = "10:00",
    val isActive: Boolean = true,
    val createdAt: Long = 0L
)