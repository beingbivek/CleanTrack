package com.example.cleantrack.model

data class ActiveTripModel(
    val tripId: String = "",
    val scheduleId: String = "",
    val routeId: String = "",
    val routeName: String = "",
    val driverId: String = "",
    val vehicleId: String = "",
    val vehicleNumber: String = "",
    val startTimestamp: Long = 0L,
    val status: String = "ACTIVE",
    val currentLat: Double = 0.0,
    val currentLng: Double = 0.0
) {
    fun toMap(): Map<String, Any?> {
        return mapOf(
            "tripId" to tripId,
            "scheduleId" to scheduleId,
            "routeId" to routeId,
            "routeName" to routeName,
            "driverId" to driverId,
            "vehicleId" to vehicleId,
            "vehicleNumber" to vehicleNumber,
            "startTimestamp" to startTimestamp,
            "status" to status,
            "currentLat" to currentLat,
            "currentLng" to currentLng
        )
    }
}
