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

    // Schedule State
    private val _isScheduleCompleted = MutableLiveData<Boolean>(false)
    val isScheduleCompleted: LiveData<Boolean> get() = _isScheduleCompleted

    private val _loading = MutableLiveData<Boolean>()
    val loading : MutableLiveData<Boolean>
        get() = _loading

    // Bin details for specific scans
    val binDetails = MutableLiveData<BinModel?>()

    // Dashboard Stats: Total, Remains, Skipped
    private val _binStats = MutableLiveData<Triple<Int, Int, Int>>()
    val binStats: LiveData<Triple<Int, Int, Int>> = _binStats

    /**
     * Start Trip with Time Validation
     * Only allows start if current time is between schedule start and end
     */
    fun startTripWithValidation(schedule: ScheduleModel, callback: (Boolean, String) -> Unit) {
        val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
        val currentTime = sdf.format(Date())

        if (currentTime >= schedule.startTime && currentTime <= schedule.endTime) {

            repo.checkExistingTrip(schedule.scheduleId) { existingTrip ->
                when {
                    // SCENARIO 1: Trip was already finished today
                    existingTrip?.status == "COMPLETED" -> {
                        callback(false, "This schedule is already completed for today.")
                    }

                    // SCENARIO 2: Trip is currently running (Resume)
                    existingTrip?.status == "ACTIVE" -> {
                        _activeTrip.postValue(existingTrip)
                        loadBinStats(existingTrip.routeId, existingTrip.tripId)
                        callback(true, "Resuming current active route.")
                    }

                    // SCENARIO 3: First time starting this schedule
                    else -> {
                        repo.startTrip(schedule.scheduleId) { success, message ->
                            if (success) {
                                repo.observeActiveTripByRoute(schedule.routeId) { trip ->
                                    _activeTrip.postValue(trip)
                                    if (trip != null) loadBinStats(trip.routeId, trip.tripId)
                                }
                            }
                            callback(success, message)
                        }
                    }
                }
            }
        } else {
            callback(false, "Cannot start. Current time ($currentTime) is outside schedule.")
        }
    }

    /**
     * Checks if a trip for this schedule was already completed today.
     * Call this when the Dashboard loads or when assignedSchedule changes.
     */
    fun checkCompletionStatus(scheduleId: String) {
        repo.checkExistingTrip(scheduleId) { trip ->
            if (trip?.status == "COMPLETED") {
                _isScheduleCompleted.postValue(true)
            } else {
                _isScheduleCompleted.postValue(false)
            }
        }
    }

    /**
     * Dashboard Data Logic
     * 1. Finds Users in Route -> 2. Finds Bins for those Users -> 3. Observes Collections for Trip
     */
    fun loadBinStats(routeId: String, tripId: String) {
        // Step A: Find users on this route
        userRepo.getUsersByRoute(routeId) { success, _, users ->
            if (success && users != null) {
                val userIds = users.map { it.userId }

                // Step B: Find all bins for those users
                binRepo.getBinsByOwnerIds(userIds) { bins ->
                    val total = bins.size

                    // Step C: Observe collection entries for this trip
                    collectionRepo.observeCollectionsByTrip(tripId) { collectionSuccess, _, collections ->
                        if (collectionSuccess && collections != null) {

                            // Every entry for this tripId counts as collected
                            val collected = collections.size
                            val remains = if (total > collected) total - collected else 0

                            // Updated Triple: Total, Collected, Remains
                            _binStats.postValue(Triple(total, collected, remains))
                        }
                    }
                }
            }
        }
    }

    /**
     * Observe an active trip by Route ID
     * Used to resume state if the app is restarted
     */
    fun observeActiveTripByRoute(routeId: String) {
        repo.observeActiveTripByRoute(routeId) { trip ->
            _activeTrip.postValue(trip)
            // If a trip is already active, resume loading stats
            trip?.let {
                loadBinStats(it.routeId, it.tripId)
            }
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

    // In ActiveTripViewModel.kt

// 1. Add this to the constructor (if not already there)
// collectionRepo: BinCollectionRepo

    fun checkAndValidateBin(tripId: String, binId: String, onResult: (Boolean, String) -> Unit) {
        loading.postValue(true) // Optional: add a loading state to this VM

        // Step 1: Check if already collected in this trip
        collectionRepo.observeCollectionsByTrip(tripId) { success, _, collections ->
            val alreadyCollected = collections?.any { it.binId == binId } ?: false

            if (alreadyCollected) {
                loading.postValue(false)
                onResult(false, "Qr is already scanned for this trip")
            } else {
                // Step 2: If not collected, fetch bin details to ensure it exists
                binRepo.getBinById(binId) { binSuccess, message, bin ->
                    loading.postValue(false)
                    if (binSuccess && bin != null) {
                        binDetails.postValue(bin)
                        onResult(true, "Success")
                    } else {
                        onResult(false, message)
                    }
                }
            }
        }
    }
}