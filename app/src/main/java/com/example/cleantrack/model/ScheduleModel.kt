package com.example.cleantrack.model

data class ScheduleModel(
    var scheduleId: String = "",
    val routeId: String = "",
    val routeName: String = "",
    val vehicleId: String = "",
    val vehicleNumber: String = "",
    val driverId: String = "",
    val dayOfWeek: String = "",   // SUNDAY, MONDAY...
    val startTime: String = "",   // HH:mm
    val endTime: String = "",     // HH:mm
    val active: Boolean = true
) {
    fun toMap(): Map<String, Any?> {
        return mapOf(
            "scheduleId" to scheduleId,
            "routeId" to routeId,
            "routeName" to routeName,
            "vehicleId" to vehicleId,
            "vehicleNumber" to vehicleNumber,
            "driverId" to driverId,
            "dayOfWeek" to dayOfWeek,
            "startTime" to startTime,
            "endTime" to endTime,
            "active" to active
        )
    }
}