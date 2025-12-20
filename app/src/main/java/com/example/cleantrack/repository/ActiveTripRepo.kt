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
}
