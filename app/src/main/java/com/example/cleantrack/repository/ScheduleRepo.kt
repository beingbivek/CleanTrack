package com.example.cleantrack.repository

import com.example.cleantrack.model.ScheduleModel

interface ScheduleRepo {

    fun addSchedule(model: ScheduleModel, callback: (Boolean, String) -> Unit)

    fun getSchedulesForDriver(
        driverId: String,
        callback: (Boolean, String, List<ScheduleModel>) -> Unit
    )

    fun getAllSchedules(
        callback: (Boolean, String, List<ScheduleModel>) -> Unit
    )

    fun deleteSchedule(scheduleId: String, callback: (Boolean, String) -> Unit)
}
