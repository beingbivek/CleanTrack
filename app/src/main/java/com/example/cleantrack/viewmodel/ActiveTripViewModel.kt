package com.example.cleantrack.viewmodel

import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.cleantrack.model.ActiveTripModel
import com.example.cleantrack.model.BinCollectionModel
import com.example.cleantrack.model.BinModel
import com.example.cleantrack.model.ScheduleModel
import com.example.cleantrack.repository.ActiveTripRepo
import com.example.cleantrack.repository.BinCollectionRepo
import com.example.cleantrack.repository.BinRepo
import com.example.cleantrack.repository.UserRepo
import com.google.android.gms.location.FusedLocationProviderClient
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ActiveTripViewModel(
    private val repo: ActiveTripRepo,
    private val userRepo: UserRepo,
    private val binRepo: BinRepo,
    private val collectionRepo: BinCollectionRepo
) : ViewModel() {

    // Active Trip State
    private val _activeTrip = MutableLiveData<ActiveTripModel?>()
    val activeTrip: LiveData<ActiveTripModel?> get() = _activeTrip

    // Bin details for specific scans
    val binDetails = MutableLiveData<BinModel?>()



    /**
     * Start Trip with Time Validation
     * Only allows start if current time is between schedule start and end
     */
    fun startTripWithValidation(schedule: ScheduleModel, callback: (Boolean, String) -> Unit) {
        val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
        val currentTime = sdf.format(Date())

        if (currentTime >= schedule.startTime && currentTime <= schedule.endTime) {
            repo.startTrip(schedule.scheduleId) { success, message ->
                if (success) {
                    // IMPORTANT: After starting, we need to find the tripId
                    // and start observing it so the LiveData updates.
                    repo.observeActiveTripByRoute(schedule.routeId) { trip ->
                        _activeTrip.postValue(trip)
                        if (trip != null) {
                            loadBinStats(trip.routeId, trip.tripId)
                        }
                    }
                }
                callback(success, message)
            }
        } else {
            callback(false, "Cannot start. Current time ($currentTime) is outside schedule.")
        }
    }


    // --- Helper Methods using Injected Repositories ---

    fun getBinById(binId: String, callback: (Boolean, String, BinModel?) -> Unit) {
        binRepo.getBinById(binId) { success, message, bin ->
            if (success) binDetails.postValue(bin)
            callback(success, message, bin)
        }
    }

    fun addBinCollection(collectionModel: BinCollectionModel, callback: (Boolean, String) -> Unit) {
        collectionRepo.addBinCollection(collectionModel) { success, message ->
            callback(success, message)
        }
    }

    fun observeTrip(tripId: String) {
        repo.observeActiveTrip(tripId) { _activeTrip.postValue(it) }
    }

    fun updateLocation(tripId: String, lat: Double, lng: Double) {
        repo.updateLocation(tripId, lat, lng) // Updates Firebase

        // Also update local LiveData so the UI shows the new coordinates
        _activeTrip.value?.let { current ->
            if (current.tripId == tripId) {
                _activeTrip.postValue(current.copy(currentLat = lat, currentLng = lng))
            }
        }
    }

    // Inside ActiveTripViewModel.kt
    fun endTrip(tripId: String, callback: (Boolean, String) -> Unit) {
        repo.endTrip(tripId) { success, msg ->
            if (success) {
                _activeTrip.postValue(null)
            }
            callback(success, msg)
        }
    }

    override fun onCleared() {
        super.onCleared()
    }
}