package com.example.cleantrack.repository

import com.example.cleantrack.model.ActiveTripModel
import com.example.cleantrack.model.ScheduleModel
import com.google.firebase.database.FirebaseDatabase

class ActiveTripRepoImpl : ActiveTripRepo {

    private val db = FirebaseDatabase.getInstance()
    private val tripRef = db.getReference("ActiveTrips")
    private val scheduleRef = db.getReference("Schedules")

    override fun startTrip(
        scheduleId: String,
        callback: (Boolean, String) -> Unit
    ) {

        scheduleRef.child(scheduleId).get()
            .addOnSuccessListener { snapshot ->
                val schedule = snapshot.getValue(ScheduleModel::class.java)
                    ?: return@addOnSuccessListener callback(false, "Schedule not found")

                // Check if driver already has an active trip
                tripRef.orderByChild("driverId")
                    .equalTo(schedule.driverId)
                    .get()
                    .addOnSuccessListener { tripSnap ->

                        for (child in tripSnap.children) {
                            val status = child.child("status").getValue(String::class.java)
                            if (status == "ACTIVE") {
                                callback(false, "You already have an active route")
                                return@addOnSuccessListener
                            }
                        }

                        // Create trip
                        val tripId = tripRef.push().key
                            ?: return@addOnSuccessListener callback(false, "Failed to start trip")

                        val trip = ActiveTripModel(
                            tripId = tripId,
                            scheduleId = schedule.scheduleId,
                            routeId = schedule.routeId,
                            routeName = schedule.routeName,
                            driverId = schedule.driverId,
                            vehicleId = schedule.vehicleId,
                            vehicleNumber = schedule.vehicleNumber,
                            startTimestamp = System.currentTimeMillis(),
                            status = "ACTIVE"
                        )

                        tripRef.child(tripId).setValue(trip)
                            .addOnSuccessListener {
                                callback(true, "Route started")
                            }
                            .addOnFailureListener {
                                callback(false, it.localizedMessage ?: "Failed")
                            }
                    }
            }
            .addOnFailureListener {
                callback(false, it.localizedMessage ?: "Error")
            }
    }

    override fun updateLocation(tripId: String, lat: Double, lng: Double) {
        tripRef.child(tripId)
            .updateChildren(
                mapOf(
                    "currentLat" to lat,
                    "currentLng" to lng
                )
            )
    }

    override fun endTrip(tripId: String, callback: (Boolean, String) -> Unit) {
        tripRef.child(tripId)
            .child("status")
            .setValue("COMPLETED")
            .addOnSuccessListener {
                callback(true, "Route completed")
            }
            .addOnFailureListener {
                callback(false, it.localizedMessage ?: "Failed")
            }
    }
}
