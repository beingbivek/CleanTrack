package com.example.cleantrack.repository

import android.util.Log
import com.example.cleantrack.model.ActiveTripModel
import com.example.cleantrack.model.ScheduleModel
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ActiveTripRepoImpl : ActiveTripRepo {

    private val db = FirebaseDatabase.getInstance()
    private val tripRef = db.getReference("ActiveTrips")
    private val scheduleRef = db.getReference("Schedules")

    override fun startTrip(scheduleId: String, callback: (Boolean, String) -> Unit) {
        Log.d("CLEANTRACK", "Repo: Fetching schedule details for $scheduleId")

        scheduleRef.child(scheduleId).get().addOnSuccessListener { snapshot ->
            val schedule = snapshot.getValue(ScheduleModel::class.java)
            if (schedule == null) {
                Log.e("CLEANTRACK", "Repo: Schedule object is NULL in Firebase")
                callback(false, "Schedule data error")
                return@addOnSuccessListener
            }

            Log.d("CLEANTRACK", "Repo: Checking for existing trips for driver: ${schedule.driverId}")

            tripRef.orderByChild("driverId").equalTo(schedule.driverId).get()
                .addOnSuccessListener { tripSnap ->
                    var alreadyHasActive = false
                    for (child in tripSnap.children) {
                        val status = child.child("status").getValue(String::class.java)
                        if (status == "ACTIVE") {
                            alreadyHasActive = true
                            break
                        }
                    }

                    if (alreadyHasActive) {
                        Log.w("CLEANTRACK", "Repo: Driver already has an active trip")
                        callback(false, "You already have an active route")
                    } else {
                        createTripRecord(schedule, callback)
                    }
                }
                .addOnFailureListener {
                    Log.e("CLEANTRACK", "Repo: Trip check failed: ${it.message}")
                    createTripRecord(schedule, callback) // Proceed anyway if check fails
                }
        }.addOnFailureListener {
            Log.e("CLEANTRACK", "Repo: Failed to get schedule: ${it.message}")
            callback(false, "Database connection error")
        }
    }

    private fun createTripRecord(schedule: ScheduleModel, callback: (Boolean, String) -> Unit) {
        val tripId = tripRef.push().key ?: return callback(false, "ID Generation Failed")

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

        Log.d("CLEANTRACK", "Repo: Writing new trip to Firebase...")
        tripRef.child(tripId).setValue(trip)
            .addOnSuccessListener {
                Log.d("CLEANTRACK", "Repo: Trip successfully created!")
                callback(true, "Route started successfully")
            }
            .addOnFailureListener {
                Log.e("CLEANTRACK", "Repo: Write failed: ${it.message}")
                callback(false, "Failed to write to database")
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
                callback(false, it.localizedMessage ?: "Failed to end route!")
            }
    }

    override fun observeActiveTrip(
        tripId: String,
        callback: (ActiveTripModel?) -> Unit
    ) {
        tripRef.child(tripId)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val trip = snapshot.getValue(ActiveTripModel::class.java)
                    callback(trip)
                }

                override fun onCancelled(error: DatabaseError) {
                    callback(null)
                }
            })
    }

    override fun observeActiveTripByRoute(
        routeId: String,
        callback: (ActiveTripModel?) -> Unit
    ) {
        tripRef.orderByChild("routeId")
            .equalTo(routeId)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {

                    for (child in snapshot.children) {
                        val trip = child.getValue(ActiveTripModel::class.java)
                        if (trip != null && trip.status == "ACTIVE") {
                            callback(trip)
                            return
                        }
                    }

                    callback(null) // No active trip
                }

                override fun onCancelled(error: DatabaseError) {
                    callback(null)
                }
            })
    }

    val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    fun getCurrentDate(): String {
        return sdf.format(Date())
    }

    override fun checkExistingTrip(scheduleId: String, callback: (ActiveTripModel?) -> Unit) {
        val today = getCurrentDate()

        // Query trips for this schedule
        tripRef.orderByChild("scheduleId").equalTo(scheduleId)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    var todayTrip: ActiveTripModel? = null

                    for (child in snapshot.children) {
                        val trip = child.getValue(ActiveTripModel::class.java)

                        // NEW: Only care about trips created TODAY
                        // Assuming your ActiveTripModel has a 'date' field or you check the timestamp
                        if (trip != null) {
                            val tripDate = sdf.format(Date(trip.startTimestamp))
                            if (tripDate == today) {
                                todayTrip = trip
                                break
                            }
                        }
                    }
                    callback(todayTrip)
                }

                override fun onCancelled(error: DatabaseError) {
                    callback(null)
                }
            })
    }


}
