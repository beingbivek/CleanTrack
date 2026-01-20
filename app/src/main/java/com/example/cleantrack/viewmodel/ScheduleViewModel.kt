package com.example.cleantrack.viewmodel

import android.content.Context
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.cleantrack.model.ScheduleModel
import com.example.cleantrack.repository.ScheduleRepo
import com.example.cleantrack.util.PreferenceManager

class ScheduleViewModel(
    private val repo: ScheduleRepo
) : ViewModel() {

    // 🔹 Single schedule (for Edit)
    private val _schedule = MutableLiveData<ScheduleModel?>()
    val schedule: MutableLiveData<ScheduleModel?>
        get() = _schedule

    // 🔹 All schedules (Admin list / Driver list)
    private val _schedules = MutableLiveData<List<ScheduleModel>?>()
    val schedules: MutableLiveData<List<ScheduleModel>?>
        get() = _schedules

    // 🔹 Loading state
    private val _loading = MutableLiveData<Boolean>()
    val loading: MutableLiveData<Boolean>
        get() = _loading

    /* -------------------- READ -------------------- */

    fun getScheduleById(scheduleId: String) {
        _loading.postValue(true)
        repo.getScheduleById(scheduleId) { success, _, data ->
            if (success) {
                _schedule.postValue(data)
            }
            _loading.postValue(false)
        }
    }

    fun getAllSchedules() {
        _loading.postValue(true)
        repo.getAllSchedules { success, _, data ->
            if (success) {
                _schedules.postValue(data)
            }
            _loading.postValue(false)
        }
    }

    fun loadDriverSchedules(driverId: String) {
        _loading.postValue(true)
        repo.getSchedulesForDriver(driverId) { success, _, data ->
            if (success) {
                _schedules.postValue(data)
            }
            _loading.postValue(false)
        }
    }

    fun hasScheduleConflict(
        newSchedule: ScheduleModel,
        existingSchedules: List<ScheduleModel>?
    ): Pair<Boolean, String> {

        val newStart = toMinutes(newSchedule.startTime)
        val newEnd = toMinutes(newSchedule.endTime)

        existingSchedules?.forEach { existing ->

            // Skip same schedule while editing
            if (existing.scheduleId == newSchedule.scheduleId) return@forEach

            if (existing.dayOfWeek != newSchedule.dayOfWeek) return@forEach

            val existingStart = toMinutes(existing.startTime)
            val existingEnd = toMinutes(existing.endTime)

            val isOverlapping =
                newStart < existingEnd && newEnd > existingStart

            if (isOverlapping) {

                // 🚚 VEHICLE CONFLICT
                if (existing.vehicleId == newSchedule.vehicleId) {
                    return Pair(
                        true,
                        "Vehicle already assigned during this time"
                    )
                }

                // 👨‍✈️ DRIVER CONFLICT
                if (existing.driverId == newSchedule.driverId) {
                    return Pair(
                        true,
                        "Driver already has a schedule during this time"
                    )
                }
            }
        }

        return Pair(false, "")
    }

    private fun toMinutes(time: String): Int {
        val parts = time.split(":")
        return parts[0].toInt() * 60 + parts[1].toInt()
    }


    /* -------------------- CREATE -------------------- */

    fun addSchedule(model: ScheduleModel, callback: (Boolean, String) -> Unit) {
        repo.addSchedule(model, callback)
    }

    /* -------------------- UPDATE -------------------- */

    fun updateSchedule(model: ScheduleModel, callback: (Boolean, String) -> Unit) {
        repo.updateSchedule(model, callback)
    }

    /* -------------------- DELETE -------------------- */

    fun deleteSchedule(scheduleId: String, callback: (Boolean, String) -> Unit) {
        repo.deleteSchedule(scheduleId, callback)
    }

    fun getScheduleByDriver(driverId: String){
        _loading.postValue(true)
        repo.getScheduleByDriver(driverId) { data ->
            _schedule.postValue(data)
            _loading.postValue(false)
        }
    }

    fun cacheSchedulesForOffline(routeId: String, context: Context) {
        val prefManager = PreferenceManager(context)
        // Use your existing repo to get schedules (ensure your repo supports filtering by route)
        repo.getAllSchedules { success, _, data ->
            if (success && data != null) {
                // Filter schedules for the user's specific route
                val userSchedules = data.filter { it.routeId == routeId }
                prefManager.saveSchedules(userSchedules)
            }
        }
    }
}
