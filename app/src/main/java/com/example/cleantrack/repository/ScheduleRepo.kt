package com.example.cleantrack.repository

import com.example.cleantrack.model.ScheduleModel

interface ScheduleRepo {

    // CREATE
    fun addSchedule(
        model: ScheduleModel,
        callback: (Boolean, String) -> Unit
    )

    // READ (Admin – single)
    fun getScheduleById(
        scheduleId: String,
        callback: (Boolean, String, ScheduleModel?) -> Unit
    )

    // READ (Admin – all)
    fun getAllSchedules(
        callback: (Boolean, String, List<ScheduleModel>?) -> Unit
    )

    // READ (Driver – assigned schedules)
    fun getSchedulesForDriver(
        driverId: String,
        callback: (Boolean, String, List<ScheduleModel>?) -> Unit
    )

    // UPDATE
    fun updateSchedule(
        model: ScheduleModel,
        callback: (Boolean, String) -> Unit
    )

    // DELETE
    fun deleteSchedule(
        scheduleId: String,
        callback: (Boolean, String) -> Unit
    )

    fun getScheduleByDriver(driverId: String, callback: (ScheduleModel?) -> Unit)
}
