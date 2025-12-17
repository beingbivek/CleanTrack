package com.example.cleantrack.repository

import com.example.cleantrack.model.ActiveTripModel

interface ActiveTripRepo {

    fun startTrip(model: ActiveTripModel, callback: (Boolean, String) -> Unit)

    fun getActiveTripForDriver(
        driverId: String,
        callback: (Boolean, String, ActiveTripModel?) -> Unit
    )

    fun getActiveTripForVehicle(
        vehicleId: String,
        callback: (Boolean, String, ActiveTripModel?) -> Unit
    )

    fun completeTrip(
        tripId: String,
        callback: (Boolean, String) -> Unit
    )
}
