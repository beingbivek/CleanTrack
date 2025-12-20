package com.example.cleantrack.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.cleantrack.model.ScheduleModel
import com.example.cleantrack.repository.ScheduleRepo

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
}
