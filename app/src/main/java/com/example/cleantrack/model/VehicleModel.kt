package com.example.cleantrack.model

data class VehicleModel(
    var vehicleId: String = "",
    val vehicleNumber: String = "",
    val type: String = "TRUCK",
    val capacity: String = "",
    val active: Boolean = true
) {
    fun toMap(): Map<String, Any?> {
        return mapOf(
            "vehicleId" to vehicleId,
            "vehicleNumber" to vehicleNumber,
            "type" to type,
            "capacity" to capacity,
            "active" to active
        )
    }
}
