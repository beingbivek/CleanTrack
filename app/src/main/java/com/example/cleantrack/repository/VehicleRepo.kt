package com.example.cleantrack.repository

import com.example.cleantrack.model.VehicleModel

interface VehicleRepo {

    fun addVehicle(
        model: VehicleModel,
        callback: (Boolean, String) -> Unit
    )

    fun getAllVehicles(
        callback: (Boolean, String, List<VehicleModel>) -> Unit
    )

    fun getVehicleById(
        vehicleId: String,
        callback: (Boolean, String, VehicleModel?) -> Unit
    )

    fun updateVehicle(
        model: VehicleModel,
        callback: (Boolean, String) -> Unit
    )

    fun deleteVehicle(
        vehicleId: String,
        callback: (Boolean, String) -> Unit
    )
}
