package com.example.cleantrack.model

data class RouteAssignmentModel(
    val assignmentId: String = "",
    val routeId: String = "",
    val driverId: String = "",
    val vehicleId: String = "",
    val daysOfWeek: List<Int> = emptyList(), // 1=Sun ... 7=Sat (or your choice)
    val startTime: String = "06:00",         // "HH:mm"
    val endTime: String = "10:00",
    val isActive: Boolean = true,
    val createdAt: Long = System.currentTimeMillis()
)
