package com.example.cleantrack.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.cleantrack.model.ScheduleModel
import com.example.cleantrack.repository.ScheduleRepo

class ScheduleViewModel(val repo: ScheduleRepo) : ViewModel() {

    private val _schedules = MutableLiveData<List<ScheduleModel>>()
    val schedules: MutableLiveData<List<ScheduleModel>>
        get() = _schedules

    fun loadDriverSchedules(driverId: String) {
        repo.getSchedulesForDriver(driverId) { success, _, data ->
            if (success) _schedules.postValue(data)
        }
    }
}
