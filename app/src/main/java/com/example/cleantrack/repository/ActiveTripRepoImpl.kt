package com.example.cleantrack.repository

import com.example.cleantrack.model.ActiveTripModel
import com.google.firebase.database.*

class ActiveTripRepoImpl : ActiveTripRepo {

    private val database = FirebaseDatabase.getInstance()
    private val ref: DatabaseReference = database.getReference("ActiveTrips")

    override fun startTrip(
        model: ActiveTripModel,
        callback: (Boolean, String) -> Unit
    ) {
        // Step 1: Check if driver already has an active trip
        ref.orderByChild("driverId").equalTo(model.driverId)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(driverSnap: DataSnapshot) {
                    for (child in driverSnap.children) {
                        val trip = child.getValue(ActiveTripModel::class.java)
                        if (trip?.status == "ACTIVE") {
                            callback(false, "Driver already has an active trip")
                            return
                        }
                    }

                    // Step 2: Check if vehicle already active
                    ref.orderByChild("vehicleId").equalTo(model.vehicleId)
                        .addListenerForSingleValueEvent(object : ValueEventListener {
                            override fun onDataChange(vehicleSnap: DataSnapshot) {
                                for (child in vehicleSnap.children) {
                                    val trip = child.getValue(ActiveTripModel::class.java)
                                    if (trip?.status == "ACTIVE") {
                                        callback(false, "Vehicle already has an active trip")
                                        return
                                    }
                                }

                                // Step 3: Create trip
                                val tripId = ref.push().key
                                    ?: return callback(false, "Failed to create trip")

                                val finalModel = model.copy(
                                    tripId = tripId,
                                    startedAt = System.currentTimeMillis()
                                )

                                ref.child(tripId).setValue(finalModel)
                                    .addOnSuccessListener {
                                        callback(true, "Trip started")
                                    }
                                    .addOnFailureListener {
                                        callback(false, it.localizedMessage ?: "Failed to start trip")
                                    }
                            }

                            override fun onCancelled(error: DatabaseError) {
                                callback(false, error.message)
                            }
                        })
                }

                override fun onCancelled(error: DatabaseError) {
                    callback(false, error.message)
                }
            })
    }

    override fun getActiveTripForDriver(
        driverId: String,
        callback: (Boolean, String, ActiveTripModel?) -> Unit
    ) {
        ref.orderByChild("driverId").equalTo(driverId)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val trip = snapshot.children
                        .mapNotNull { it.getValue(ActiveTripModel::class.java) }
                        .firstOrNull { it.status == "ACTIVE" }

                    callback(true, "Fetched", trip)
                }

                override fun onCancelled(error: DatabaseError) {
                    callback(false, error.message, null)
                }
            })
    }

    override fun getActiveTripForVehicle(
        vehicleId: String,
        callback: (Boolean, String, ActiveTripModel?) -> Unit
    ) {
        ref.orderByChild("vehicleId").equalTo(vehicleId)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val trip = snapshot.children
                        .mapNotNull { it.getValue(ActiveTripModel::class.java) }
                        .firstOrNull { it.status == "ACTIVE" }

                    callback(true, "Fetched", trip)
                }

                override fun onCancelled(error: DatabaseError) {
                    callback(false, error.message, null)
                }
            })
    }

    override fun completeTrip(
        tripId: String,
        callback: (Boolean, String) -> Unit
    ) {
        ref.child(tripId).child("status").setValue("COMPLETED")
            .addOnSuccessListener {
                callback(true, "Trip completed")
            }
            .addOnFailureListener {
                callback(false, it.localizedMessage ?: "Failed to complete trip")
            }
    }
}
