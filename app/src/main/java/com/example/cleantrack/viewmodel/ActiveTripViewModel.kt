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
import com.example.cleantrack.model.TripHistoryUiModel
import com.example.cleantrack.repository.ActiveTripRepo
import com.example.cleantrack.repository.BinCollectionRepo
import com.example.cleantrack.repository.BinRepo
import com.example.cleantrack.repository.PointsRepo
import com.example.cleantrack.repository.UserRepo
import com.google.android.gms.location.FusedLocationProviderClient
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ActiveTripViewModel(
    private val repo: ActiveTripRepo,
    private val userRepo: UserRepo,
    private val binRepo: BinRepo,
    private val collectionRepo: BinCollectionRepo,
    private val pointsRepo: PointsRepo // ADD THIS
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
                    existingTrip?.status == "COMPLETED" -> {
                        // --- NEW LOGIC: Check if all bins are already collected ---
                        userRepo.getUsersByRoute(schedule.routeId) { success, _, users ->
                            if (success && users != null) {
                                val userIds = users.map { it.userId }
                                binRepo.getBinsByOwnerIds(userIds) { bins ->
                                    val totalBins = bins.size

                                    collectionRepo.observeCollectionsByTrip(existingTrip.tripId) { collSuccess, _, collections ->
                                        val collectedCount = collections?.size ?: 0

                                        if (collectedCount >= totalBins && totalBins > 0) {
                                            // Block restart because work is finished
                                            callback(false, "Route fully collected. Cannot restart.")
                                        } else {
                                            // Proceed with restart as some bins remain
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
                    // ... Scenario 2 (ACTIVE) and Scenario 3 (New Trip) remain the same
                    existingTrip?.status == "ACTIVE" -> {
                        _activeTrip.postValue(existingTrip)
                        loadBinStats(existingTrip.routeId, existingTrip.tripId)
                        callback(true, "Resuming active route.")
                    }
                    else -> {
                        repo.startTrip(schedule.scheduleId) { s, m ->
                            if (s) observeActiveTripByRoute(schedule.routeId)
                            callback(s, m)
                        }
                    }
                }
            }
        } else {
            callback(false, "Outside schedule time.")
        }
    }

    /**
     * Checks if a trip for this schedule was already completed today.
     * Call this when the Dashboard loads or when assignedSchedule changes.
     */
    fun checkCompletionStatus(scheduleId: String) {
        repo.checkExistingTrip(scheduleId) { trip ->
            // If there is an active trip, it is definitely NOT completed.
            if (trip?.status == "ACTIVE") {
                _isScheduleCompleted.postValue(false)
            } else if (trip?.status == "COMPLETED") {
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

    fun checkAndValidateBin(tripId: String, currentRouteId: String, binId: String, onResult: (Boolean, String) -> Unit) {
        _loading.postValue(true)

        // CHANGE: Use the 'Once' method here
        collectionRepo.getCollectionsByTripOnce(tripId) { success, _, collections ->
            val alreadyCollected = collections?.any { it.binId == binId } ?: false

            if (alreadyCollected) {
                _loading.postValue(false)
                onResult(false, "This bin has already been scanned for this trip.")
                return@getCollectionsByTripOnce // Stops execution here
            }

            // STEP 2: Fetch Bin Details
            binRepo.getBinById(binId) { binSuccess, _, bin ->
                if (!binSuccess || bin == null) {
                    _loading.postValue(false)
                    onResult(false, "Invalid QR Code: Bin not found in system.")
                    return@getBinById
                }

                // STEP 3: Fetch the Owner of this bin to check their route
                userRepo.getUserById(bin.ownerUserId) { userSuccess, _, owner ->
                    _loading.postValue(false)

                    if (userSuccess && owner != null) {
                        // STEP 4: The Route Validation
                        if (owner.activeRouteId == currentRouteId) {
                            binDetails.postValue(bin)
                            onResult(true, "Success")
                        } else {
                            onResult(false, "Access Denied: This bin belongs to Route ${owner.activeRouteId}, not your current route.")
                        }
                    } else {
                        onResult(false, "Error: Could not verify bin owner.")
                    }
                }
            }
        }
    }
    // --- DELETE THIS BROKEN FUNCTION ---
// fun addBinCollection(collectionModel: BinCollectionModel, callback: (Boolean, String) -> Unit) { ... }

    // --- USE THIS CORRECTED VERSION ---
    fun collectBinWithPoints(
        bin: BinModel,
        driverId: String,
        tripId: String,
        rating: Int,
        remarks: String,
        isSegregated: Boolean,
        callback: (Boolean, String) -> Unit
    ) {
        _loading.postValue(true)

        // 1. Fetch the user to ensure they exist and get their route info
        userRepo.getUserById(bin.ownerUserId) { success, message, user ->
            if (!success || user == null) {
                _loading.postValue(false)
                callback(false, "User not found: $message")
                return@getUserById
            }

            // 2. Get route ID (matching your UserModel field name)
            val userRouteId = user.activeRouteId ?: ""

            // 3. Get the point value from the rules engine
            pointsRepo.calculatePoints(bin.category, isSegregated) { calculatedPoints ->

                // 4. Create the collection record
                val collection = BinCollectionModel(
                    binId = bin.binId,
                    driverId = driverId,
                    userId = bin.ownerUserId,
                    tripId = tripId,
                    rating = rating,
                    remarks = remarks,
                    segregatedCorrectly = isSegregated,
                    pointsAwarded = calculatedPoints,
                    collectedAt = System.currentTimeMillis()
                )

                // 5. Save the collection log
                collectionRepo.addBinCollection(collection) { saveSuccess, saveMessage ->
                    if (saveSuccess) {
                        // 6. Award the points to the user's balance
                        pointsRepo.addPointsToUser(bin.ownerUserId, calculatedPoints)

                        // 7. Refresh the Dashboard numbers (Total/Remains/Collected)
                        if (userRouteId.isNotEmpty()) {
                            loadBinStats(userRouteId, tripId)
                        }
                    }
                    _loading.postValue(false)
                    callback(saveSuccess, saveMessage)
                }
            }
        }
    }

    private val _tripHistory = MutableLiveData<List<TripHistoryUiModel>>()
    val tripHistory: LiveData<List<TripHistoryUiModel>> get() = _tripHistory

    fun fetchDriverHistory(driverId: String) {
        _loading.postValue(true)
        repo.getDriverTripHistory(driverId) { success, message, trips ->
            if (success && !trips.isNullOrEmpty()) {
                val historyList = mutableListOf<TripHistoryUiModel>()
                var processedCount = 0

                trips.forEach { trip ->
                    // STEP 1: Get the Driver's Name
                    userRepo.getUserById(trip.driverId) { userSuccess, _, driverUser ->
                        val dName = driverUser?.fullname ?: "Unknown Driver"

                        // STEP 2: Get users on the route to count bins
                        userRepo.getUsersByRoute(trip.routeId) { _, _, routeUsers ->
                            val userIds = routeUsers?.map { it.userId } ?: emptyList()

                            binRepo.getBinsByOwnerIds(userIds) { bins ->
                                val total = bins.size

                                // STEP 3: Get collections for this specific trip
                                collectionRepo.getCollectionsByTripOnce(trip.tripId) { _, _, collections ->
                                    val collected = collections?.size ?: 0

                                    historyList.add(TripHistoryUiModel(
                                        trip = trip,
                                        totalBins = total,
                                        collectedBins = collected,
                                        driverName = dName // Save the name here
                                    ))

                                    processedCount++
                                    if (processedCount == trips.size) {
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

    fun collectBinWithAI(
        bin: BinModel,
        driverId: String,
        tripId: String,
        rating: Int,
        remarks: String,
        aiTip: String, // Matches the new AI tip parameter from your Activity
        isSegregated: Boolean,
        callback: (Boolean, String) -> Unit
    ) {
        _loading.postValue(true)

        // 1. Fetch the user to verify ownership and route
        userRepo.getUserById(bin.ownerUserId) { success, message, user ->
            if (!success || user == null) {
                _loading.postValue(false)
                callback(false, "User not found: $message")
                return@getUserById
            }

            val userRouteId = user.activeRouteId ?: ""

            // 2. Calculate points using the Points Repository rules
            pointsRepo.calculatePoints(bin.category, isSegregated) { calculatedPoints ->

                // 3. Construct the record with the AI feedback tip
                val collection = BinCollectionModel(
                    binId = bin.binId,
                    driverId = driverId,
                    userId = bin.ownerUserId,
                    tripId = tripId,
                    rating = rating,
                    remarks = remarks,
                    aiFeedback = aiTip, // 🔹 SAVES THE GEMINI TIP TO DB
                    segregatedCorrectly = isSegregated,
                    pointsAwarded = calculatedPoints,
                    collectedAt = System.currentTimeMillis()
                )

                // 4. Save the collection log to the database
                collectionRepo.addBinCollection(collection) { saveSuccess, saveMessage ->
                    if (saveSuccess) {
                        // 5. Update user's point balance
                        pointsRepo.addPointsToUser(bin.ownerUserId, calculatedPoints)

                        // 6. Refresh Dashboard Stats (Collected vs Remains)
                        if (userRouteId.isNotEmpty()) {
                            loadBinStats(userRouteId, tripId)
                        }
                    }
                    _loading.postValue(false)
                    callback(saveSuccess, saveMessage)
                }
            }
        }
    }
}