package com.example.cleantrack.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.cleantrack.model.ActiveTripModel
import com.example.cleantrack.model.BinCollectionModel
import com.example.cleantrack.model.BinModel
import com.example.cleantrack.repository.ActiveTripRepo
import com.example.cleantrack.repository.BinCollectionRepoImpl
import com.example.cleantrack.repository.BinRepoImpl

class ActiveTripViewModel(val repo: ActiveTripRepo) : ViewModel() {
    // Existing LiveData for active trip
//    val activeTrip = MutableLiveData<ActiveTripModel>()
    private val _activeTrip = MutableLiveData<ActiveTripModel?>()
    val activeTrip: LiveData<ActiveTripModel?> get() = _activeTrip

    // New LiveData for bin details
    val binDetails = MutableLiveData<BinModel?>()

    // New method to get bin details
    fun getBinById(binId: String, callback: (Boolean, String, BinModel?) -> Unit) {
        // Assuming BinRepoImpl is already available
        val binRepo = BinRepoImpl()
        binRepo.getBinById(binId) { success, message, bin ->
            if (success) {
                binDetails.postValue(bin)
            }
            callback(success, message, bin)
        }
    }

    fun addBinCollection(collectionModel: BinCollectionModel, callback: (Boolean, String) -> Unit) {
        val binCollectionRepo = BinCollectionRepoImpl()
        binCollectionRepo.addBinCollection(collectionModel) { success, message ->
            callback(success, message)
        }
    }

    fun observeTrip(tripId: String) {
        repo.observeActiveTrip(tripId) {
            _activeTrip.postValue(it)
        }
    }

    fun observeActiveTripByRoute(routeId: String) {
        repo.observeActiveTripByRoute(routeId) {
            _activeTrip.postValue(it)
        }
    }

    fun startTrip(scheduleId: String, callback: (Boolean, String) -> Unit) {
        repo.startTrip(scheduleId, callback)
    }

    fun updateLocation(tripId: String, lat: Double, lng: Double) {
        repo.updateLocation(tripId, lat, lng)
    }

    fun endTrip(tripId: String, callback: (Boolean, String) -> Unit) {
        repo.endTrip(tripId, callback)
    }
}
