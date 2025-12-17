package com.example.cleantrack.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.cleantrack.model.schedule.ScheduleModel
import com.example.cleantrack.repository.ScheduleRepo

class ScheduleViewModel(val repo: ScheduleRepo) : ViewModel() {

    private val _schedules = MutableLiveData<List<ScheduleModel>>()
    val schedules: MutableLiveData<List<ScheduleModel>>
        get() = _schedules

    private val _loading = MutableLiveData<Boolean>()
    val loading: MutableLiveData<Boolean>
        get() = _loading

    fun loadAllSchedules() {
        _loading.postValue(true)
        repo.getAllSchedules { success, _, data ->
            _loading.postValue(false)
            if (success) _schedules.postValue(data)
        }
    }

    fun loadDriverSchedules(driverId: String) {
        _loading.postValue(true)
        repo.getSchedulesForDriver(driverId) { success, _, data ->
            _loading.postValue(false)
            if (success) _schedules.postValue(data)
        }
    }

    fun createSchedule(model: ScheduleModel, callback: (Boolean, String) -> Unit) {
        repo.createSchedule(model, callback)
    }

    fun deactivateSchedule(scheduleId: String, callback: (Boolean, String) -> Unit) {
        repo.deactivateSchedule(scheduleId, callback)
    }
}
