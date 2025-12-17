package com.example.cleantrack.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.cleantrack.model.ActiveTripModel
import com.example.cleantrack.repository.ActiveTripRepo

class ActiveTripViewModel(val repo: ActiveTripRepo) : ViewModel() {

    private val _activeTrip = MutableLiveData<ActiveTripModel?>()
    val activeTrip: MutableLiveData<ActiveTripModel?>
        get() = _activeTrip

    private val _loading = MutableLiveData<Boolean>()
    val loading: MutableLiveData<Boolean>
        get() = _loading

    fun loadActiveTripForDriver(driverId: String) {
        _loading.postValue(true)
        repo.getActiveTripForDriver(driverId) { success, _, data ->
            _loading.postValue(false)
            if (success) _activeTrip.postValue(data)
        }
    }

    fun startTrip(model: ActiveTripModel, callback: (Boolean, String) -> Unit) {
        repo.startTrip(model, callback)
    }

    fun completeTrip(tripId: String, callback: (Boolean, String) -> Unit) {
        repo.completeTrip(tripId, callback)
    }
}
