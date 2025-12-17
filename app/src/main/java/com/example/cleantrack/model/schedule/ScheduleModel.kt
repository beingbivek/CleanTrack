package com.example.cleantrack.model

data class ScheduleModel(
    val scheduleId: String = "",
    val routeId: String = "",
    val routeName: String = "",
    val driverId: String = "",
    val driverName: String = "",
    val vehicleId: String = "",
    val vehicleNumber: String = "",
    val daysOfWeek: List<Int> = emptyList(), // 1=Sun ... 7=Sat
    val startTime: String = "",
    val endTime: String = "",
    val isActive: Boolean = true,
    val createdAt: Long = 0L
) {
    fun toMap(): Map<String, Any?> {
        return mapOf(
            "scheduleId" to scheduleId,
            "routeId" to routeId,
            "routeName" to routeName,
            "driverId" to driverId,
            "driverName" to driverName,
            "vehicleId" to vehicleId,
            "vehicleNumber" to vehicleNumber,
            "daysOfWeek" to daysOfWeek,
            "startTime" to startTime,
            "endTime" to endTime,
            "isActive" to isActive,
            "createdAt" to createdAt
        )
    }
}
