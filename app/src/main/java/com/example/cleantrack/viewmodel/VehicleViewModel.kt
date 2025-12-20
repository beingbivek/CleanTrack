package com.example.cleantrack.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.cleantrack.model.VehicleModel
import com.example.cleantrack.repository.VehicleRepo

class VehicleViewModel(
    private val repo: VehicleRepo
) : ViewModel() {

    val vehicles = MutableLiveData<List<VehicleModel>>(emptyList())
    val vehicle = MutableLiveData<VehicleModel?>()
    val loading = MutableLiveData(false)

    fun getAllVehicles() {
        loading.postValue(true)
        repo.getAllVehicles { success, _, data ->
            if (success) vehicles.postValue(data)
            loading.postValue(false)
        }
    }

    fun getVehicleById(vehicleId: String) {
        loading.postValue(true)
        repo.getVehicleById(vehicleId) { success, _, data ->
            if (success) vehicle.postValue(data)
            loading.postValue(false)
        }
    }

    fun addVehicle(model: VehicleModel, callback: (Boolean, String) -> Unit) {
        repo.addVehicle(model, callback)
    }

    fun updateVehicle(model: VehicleModel, callback: (Boolean, String) -> Unit) {
        repo.updateVehicle(model, callback)
    }

    fun deleteVehicle(vehicleId: String, callback: (Boolean, String) -> Unit) {
        repo.deleteVehicle(vehicleId, callback)
    }
}
