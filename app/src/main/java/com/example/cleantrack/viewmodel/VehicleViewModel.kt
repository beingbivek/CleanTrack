package com.example.cleantrack.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.cleantrack.model.VehicleModel
import com.example.cleantrack.repository.VehicleRepo

class VehicleViewModel(val repo: VehicleRepo) : ViewModel() {

    private val _vehicles = MutableLiveData<List<VehicleModel>>()
    val vehicles: MutableLiveData<List<VehicleModel>>
        get() = _vehicles

    fun loadVehicles() {
        repo.getAllVehicles { success, _, data ->
            if (success) _vehicles.postValue(data)
        }
    }

    fun addVehicle(model: VehicleModel, callback: (Boolean, String) -> Unit) {
        repo.addVehicle(model, callback)
    }
}
