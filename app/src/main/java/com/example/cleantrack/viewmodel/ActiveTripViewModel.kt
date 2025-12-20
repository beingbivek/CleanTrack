package com.example.cleantrack.viewmodel

import androidx.lifecycle.ViewModel
import com.example.cleantrack.repository.ActiveTripRepo

class ActiveTripViewModel(val repo: ActiveTripRepo) : ViewModel() {

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
