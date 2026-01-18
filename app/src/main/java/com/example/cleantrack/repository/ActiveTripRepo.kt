package com.example.cleantrack.repository

import com.example.cleantrack.model.ActiveTripModel

interface ActiveTripRepo {

    fun startTrip(
        scheduleId: String,
        callback: (Boolean, String) -> Unit
    )

    fun updateLocation(
        tripId: String,
        lat: Double,
        lng: Double
    )

    fun endTrip(tripId: String, callback: (Boolean, String) -> Unit)

    fun observeActiveTrip(
        tripId: String,
        callback: (ActiveTripModel?) -> Unit
    )

    fun observeActiveTripByRoute(
        routeId: String,
        callback: (ActiveTripModel?) -> Unit
    )

    fun checkExistingTrip(scheduleId: String, callback: (ActiveTripModel?) -> Unit)

    fun resumeTrip(tripId: String, callback: (Boolean, String) -> Unit)

    fun getDriverTripHistory(driverId: String, callback: (Boolean, String, List<ActiveTripModel>?) -> Unit)
}
