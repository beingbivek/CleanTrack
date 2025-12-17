package com.example.cleantrack.model

data class ActiveTripModel(
    val tripId: String = "",
    val routeId: String = "",
    val routeName: String = "",
    val vehicleId: String = "",
    val vehicleNumber: String = "",
    val driverId: String = "",
    val driverName: String = "",
    val startedAt: Long = 0L,
    val status: String = "ACTIVE" // ACTIVE | COMPLETED
) {
    fun toMap(): Map<String, Any?> {
        return mapOf(
            "tripId" to tripId,
            "routeId" to routeId,
            "routeName" to routeName,
            "vehicleId" to vehicleId,
            "vehicleNumber" to vehicleNumber,
            "driverId" to driverId,
            "driverName" to driverName,
            "startedAt" to startedAt,
            "status" to status
        )
    }
}
