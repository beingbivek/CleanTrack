package com.example.cleantrack.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.cleantrack.model.ActiveTripModel
import com.example.cleantrack.model.BinCollectionModel
import com.example.cleantrack.model.BinModel
import com.example.cleantrack.model.ScheduleModel
import com.example.cleantrack.model.TripHistoryUiModel
import com.example.cleantrack.repository.*
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ActiveTripViewModel(
    private val repo: ActiveTripRepo,
    private val userRepo: UserRepo,
    private val binRepo: BinRepo,
    private val collectionRepo: BinCollectionRepo,
    private val pointsRepo: PointsRepo
) : ViewModel() {

    private val _activeTrip = MutableLiveData<ActiveTripModel?>()
    val activeTrip: LiveData<ActiveTripModel?> get() = _activeTrip

    private val _isScheduleCompleted = MutableLiveData<Boolean>(false)
    val isScheduleCompleted: LiveData<Boolean> get() = _isScheduleCompleted

    private val _loading = MutableLiveData<Boolean>(false)
    val loading: LiveData<Boolean> get() = _loading

    val binDetails = MutableLiveData<BinModel?>()

    private val _binStats = MutableLiveData<Triple<Int, Int, Int>>()
    val binStats: LiveData<Triple<Int, Int, Int>> = _binStats

    private val _tripHistory = MutableLiveData<List<TripHistoryUiModel>>()
    val tripHistory: LiveData<List<TripHistoryUiModel>> get() = _tripHistory

    fun startTripWithValidation(schedule: ScheduleModel, callback: (Boolean, String) -> Unit) {
        val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
        val currentTime = sdf.format(Date())

        // 1. Check if Shift has expired
        if (currentTime > schedule.endTime) {
            repo.checkExistingTrip(schedule.scheduleId) { existingTrip ->
                if (existingTrip?.status == "ACTIVE") {
                    repo.endTrip(existingTrip.tripId) { _, _ ->
                        _activeTrip.postValue(existingTrip.copy(status = "COMPLETED"))
                    }
                }
            }
            callback(false, "Shift ended at ${schedule.endTime}. You cannot start or resume now.")
            return
        }

        // 2. Check if it's too early
        if (currentTime < schedule.startTime) {
            callback(false, "Route starts at ${schedule.startTime}.")
            return
        }

        // 3. Main Logic for Valid Time Window
        repo.checkExistingTrip(schedule.scheduleId) { existingTrip ->
            when {
                // CASE: Trip was completed but driver wants to RESUME
                existingTrip?.status == "COMPLETED" -> {
                    userRepo.getUsersByRoute(schedule.routeId) { success, _, users ->
                        if (success && users != null) {
                            val userIds = users.map { it.userId }
                            binRepo.getBinsByOwnerIds(userIds) { bins ->
                                collectionRepo.getCollectionsByTripOnce(existingTrip.tripId) { _, _, collections ->
                                    if ((collections?.size ?: 0) >= bins.size && bins.isNotEmpty()) {
                                        callback(false, "Route fully collected. Cannot restart.")
                                    } else {
                                        repo.resumeTrip(existingTrip.tripId) { s, m ->
                                            if (s) {
                                                val resumed = existingTrip.copy(status = "ACTIVE")
                                                _activeTrip.postValue(resumed)
                                                loadBinStats(resumed.routeId, resumed.tripId)
                                            }
                                            callback(s, m)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // CASE: Trip is already ACTIVE (syncing state)
                existingTrip?.status == "ACTIVE" -> {
                    _activeTrip.postValue(existingTrip)
                    loadBinStats(existingTrip.routeId, existingTrip.tripId)
                    callback(true, "Resuming active route.")
                }

                // CASE: No trip exists for today, START NEW
                else -> {
                    repo.startTrip(schedule.scheduleId) { s, m ->
                        if (s) observeActiveTripByRoute(schedule.routeId)
                        callback(s, m)
                    }
                }
            }
        }
    }

    fun endTrip(tripId: String, callback: (Boolean, String) -> Unit) {
        repo.endTrip(tripId) { success, msg ->
            if (success) {
                _activeTrip.value?.let { current ->
                    if (current.tripId == tripId) {
                        // Update status to COMPLETED instead of null so "Resume" shows
                        _activeTrip.postValue(current.copy(status = "COMPLETED"))
                    }
                }
            }
            callback(success, msg)
        }
    }

    fun loadBinStats(routeId: String, tripId: String) {
        userRepo.getUsersByRoute(routeId) { success, _, users ->
            if (success && users != null) {
                val userIds = users.map { it.userId }
                binRepo.getBinsByOwnerIds(userIds) { bins ->
                    val total = bins.size
                    collectionRepo.observeCollectionsByTrip(tripId) { colSuccess, _, collections ->
                        if (colSuccess && collections != null) {
                            val collected = collections.size
                            val remains = if (total > collected) total - collected else 0
                            _binStats.postValue(Triple(total, collected, remains))
                        }
                    }
                }
            }
        }
    }

    fun observeActiveTripByRoute(routeId: String) {
        repo.observeActiveTripByRoute(routeId) { trip ->
            _activeTrip.postValue(trip)
            trip?.let { loadBinStats(it.routeId, it.tripId) }
        }
    }

    fun updateLocation(tripId: String, lat: Double, lng: Double) {
        repo.updateLocation(tripId, lat, lng)
        _activeTrip.value?.let { current ->
            if (current.tripId == tripId) {
                _activeTrip.postValue(current.copy(currentLat = lat, currentLng = lng))
            }
        }
    }

    fun checkAndValidateBin(tripId: String, currentRouteId: String, binId: String, onResult: (Boolean, String) -> Unit) {
        _loading.postValue(true)
        collectionRepo.getCollectionsByTripOnce(tripId) { success, _, collections ->
            val alreadyCollected = collections?.any { it.binId == binId } ?: false
            if (alreadyCollected) {
                _loading.postValue(false)
                onResult(false, "This bin has already been scanned for this trip.")
                return@getCollectionsByTripOnce
            }

            binRepo.getBinById(binId) { binSuccess, _, bin ->
                if (!binSuccess || bin == null) {
                    _loading.postValue(false)
                    onResult(false, "Invalid QR Code: Bin not found.")
                    return@getBinById
                }

                userRepo.getUserById(bin.ownerUserId) { userSuccess, _, owner ->
                    _loading.postValue(false)
                    if (userSuccess && owner != null) {
                        if (owner.activeRouteId == currentRouteId) {
                            binDetails.postValue(bin)
                            onResult(true, "Success")
                        } else {
                            onResult(false, "This bin belongs to Route ${owner.activeRouteId}.")
                        }
                    } else {
                        onResult(false, "Error verifying bin owner.")
                    }
                }
            }
        }
    }

    fun collectBinWithAI(
        bin: BinModel, driverId: String, tripId: String, rating: Int,
        weight: Double, remarks: String, aiTip: String, isSegregated: Boolean,
        callback: (Boolean, String) -> Unit
    ) {
        _loading.postValue(true)
        userRepo.getUserById(bin.ownerUserId) { success, _, user ->
            if (!success || user == null) {
                _loading.postValue(false)
                callback(false, "User not found")
                return@getUserById
            }

            pointsRepo.calculatePoints(bin.category, isSegregated) { points ->
                val collection = BinCollectionModel(
                    binId = bin.binId, driverId = driverId, userId = bin.ownerUserId,
                    tripId = tripId, rating = rating, weight = weight, remarks = remarks,
                    aiFeedback = aiTip, segregatedCorrectly = isSegregated,
                    pointsAwarded = points, collectedAt = System.currentTimeMillis()
                )

                collectionRepo.addBinCollection(collection) { saveS, saveM ->
                    if (saveS) {
                        pointsRepo.addPointsToUser(bin.ownerUserId, points)
                        loadBinStats(user.activeRouteId ?: "", tripId)
                    }
                    _loading.postValue(false)
                    callback(saveS, saveM)
                }
            }
        }
    }

    fun fetchDriverHistory(driverId: String) {
        _loading.postValue(true)
        repo.getDriverTripHistory(driverId) { success, _, trips ->
            if (success && !trips.isNullOrEmpty()) {
                val historyList = mutableListOf<TripHistoryUiModel>()
                var processed = 0
                trips.forEach { trip ->
                    userRepo.getUserById(trip.driverId) { _, _, driver ->
                        userRepo.getUsersByRoute(trip.routeId) { _, _, routeUsers ->
                            val userIds = routeUsers?.map { it.userId } ?: emptyList()
                            binRepo.getBinsByOwnerIds(userIds) { bins ->
                                collectionRepo.getCollectionsByTripOnce(trip.tripId) { _, _, collections ->
                                    historyList.add(TripHistoryUiModel(trip, bins.size, collections?.size ?: 0, driver?.fullname ?: "Unknown"))
                                    processed++
                                    if (processed == trips.size) {
                                        _tripHistory.postValue(historyList.sortedByDescending { it.trip.startTimestamp })
                                        _loading.postValue(false)
                                    }
                                }
                            }
                        }
                    }
                }
            } else {
                _tripHistory.postValue(emptyList())
                _loading.postValue(false)
            }
        }
    }

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
}

