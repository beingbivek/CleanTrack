package com.example.cleantrack.model

data class BinCollectionModel(
    var collectionId: String = "",
    var binId: String = "",
    var driverId: String = "",
    var userId: String = "", // User who owns the bin
    var tripId: String = "",
    var rating: Int = 0, // Rating given by the driver (0-5)
    var weight: Double = 0.0,
    var remarks: String = "",
    val aiFeedback: String = "",
    var segregatedCorrectly: Boolean = false, // Is it segregated correctly?
    var pointsAwarded: Int = 0, // Points calculated for the user
    var collectedAt: Long = System.currentTimeMillis()
) {
    fun toMap(): Map<String, Any?> {
        return mapOf(
            "collectionId" to collectionId,
            "binId" to binId,
            "driverId" to driverId,
            "userId" to userId,
            "tripId" to tripId,
            "rating" to rating,
            "weight" to weight,
            "remarks" to remarks,
            "aiFeedback" to aiFeedback,
            "segregatedCorrectly" to segregatedCorrectly,
            "pointsAwarded" to pointsAwarded,
            "collectedAt" to collectedAt
        )
    }
}
