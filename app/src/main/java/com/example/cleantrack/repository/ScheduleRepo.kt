package com.example.cleantrack.repository

import com.example.cleantrack.model.schedule.ScheduleModel

interface ScheduleRepo {

    fun createSchedule(model: ScheduleModel, callback: (Boolean, String) -> Unit)

    fun getAllSchedules(callback: (Boolean, String, List<ScheduleModel>) -> Unit)

    fun getSchedulesForDriver(
        driverId: String,
        callback: (Boolean, String, List<ScheduleModel>) -> Unit
    )

    fun updateSchedule(
        scheduleId: String,
        model: ScheduleModel,
        callback: (Boolean, String) -> Unit
    )

    fun deactivateSchedule(
        scheduleId: String,
        callback: (Boolean, String) -> Unit
    )
}
