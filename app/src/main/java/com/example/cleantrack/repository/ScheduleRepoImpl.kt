package com.example.cleantrack.repository

import com.example.cleantrack.model.ScheduleModel
import com.google.firebase.database.*
import com.example.cleantrack.util.NotificationTypes
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ScheduleRepoImpl : ScheduleRepo {

    private val database: FirebaseDatabase = FirebaseDatabase.getInstance()
    private val ref: DatabaseReference = database.getReference("Schedules")
    private val notificationRepo = NotificationRepoImpl()
    private val userRepo = UserRepoImpl()

    /* -------------------- CREATE -------------------- */

    override fun addSchedule(
        model: ScheduleModel,
        callback: (Boolean, String) -> Unit
    ) {
        model.scheduleId = ref.push().key ?: ""
        ref.child(model.scheduleId)
            .setValue(model)
            .addOnCompleteListener {
                if (it.isSuccessful) {
                    notifyScheduleChange(
                        model,
                        NotificationTypes.SCHEDULE_ADDED,
                        "Schedule added",
                        "New pickup schedule for ${model.routeName} on ${model.dayOfWeek} at ${model.startTime}."
                    )
                    callback(true, "Schedule added successfully")
                } else {
                    callback(false, it.exception?.message ?: "Error")
                }
            }
    }

    /* -------------------- READ (SINGLE) -------------------- */

    override fun getScheduleById(
        scheduleId: String,
        callback: (Boolean, String, ScheduleModel?) -> Unit
    ) {
        ref.child(scheduleId)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        val schedule =
                            snapshot.getValue(ScheduleModel::class.java)
                        callback(true, "Schedule fetched", schedule)
                    } else {
                        callback(false, "Schedule not found", null)
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    callback(false, error.message, null)
                }
            })
    }

    /* -------------------- READ (ALL – ADMIN) -------------------- */

    override fun getAllSchedules(
        callback: (Boolean, String, List<ScheduleModel>?) -> Unit
    ) {
        ref.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {

                val list = mutableListOf<ScheduleModel>()

                for (child in snapshot.children) {
                    val schedule =
                        child.getValue(ScheduleModel::class.java)
                    if (schedule != null) list.add(schedule)
                }

                callback(true, "Schedules fetched", list)
            }

            override fun onCancelled(error: DatabaseError) {
                callback(false, error.message, null)
            }
        })
    }

    /* -------------------- READ (DRIVER) -------------------- */

    override fun getSchedulesForDriver(
        driverId: String,
        callback: (Boolean, String, List<ScheduleModel>?) -> Unit
    ) {
        ref.orderByChild("driverId")
            .equalTo(driverId)
            .addListenerForSingleValueEvent(object : ValueEventListener {

                override fun onDataChange(snapshot: DataSnapshot) {

                    val list = mutableListOf<ScheduleModel>()

                    for (child in snapshot.children) {
                        val schedule =
                            child.getValue(ScheduleModel::class.java)
                        if (schedule != null) list.add(schedule)
                    }

                    callback(true, "Driver schedules fetched", list)
                }

                override fun onCancelled(error: DatabaseError) {
                    callback(false, error.message, null)
                }
            })
    }

    /* -------------------- UPDATE -------------------- */

    override fun updateSchedule(
        model: ScheduleModel,
        callback: (Boolean, String) -> Unit
    ) {
        ref.child(model.scheduleId)
            .setValue(model)
            .addOnCompleteListener {
                if (it.isSuccessful) {
                    notifyScheduleChange(
                        model,
                        NotificationTypes.SCHEDULE_UPDATED,
                        "Schedule updated",
                        "Pickup schedule updated for ${model.routeName} on ${model.dayOfWeek}."
                    )
                    callback(true, "Schedule updated successfully")
                } else {
                    callback(false, it.exception?.message ?: "Error")
                }
            }
    }

    /* -------------------- DELETE -------------------- */

    override fun deleteSchedule(
        scheduleId: String,
        callback: (Boolean, String) -> Unit
    ) {
        ref.child(scheduleId).get().addOnSuccessListener { snapshot ->
            val schedule = snapshot.getValue(ScheduleModel::class.java)
            ref.child(scheduleId)
                .removeValue()
                .addOnCompleteListener {
                    if (it.isSuccessful) {
                        schedule?.let {
                            notifyScheduleChange(
                                it,
                                NotificationTypes.SCHEDULE_DELETED,
                                "Schedule deleted",
                                "Pickup schedule for ${it.routeName} on ${it.dayOfWeek} was removed."
                            )
                        }
                        callback(true, "Schedule deleted successfully")
                    } else {
                        callback(false, it.exception?.message ?: "Error")
                    }
                }
        }.addOnFailureListener { error ->
            callback(false, error.message ?: "Error")
        }
    }

    override fun getScheduleByDriver(driverId: String, callback: (ScheduleModel?) -> Unit) {
        val scheduleRef = FirebaseDatabase.getInstance().getReference("Schedules")

        // Get current day and time
        val dayFormat = SimpleDateFormat("EEEE", Locale.getDefault())
        val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
        val todayName = dayFormat.format(Date())
        val currentTime = timeFormat.format(Date())

        scheduleRef.orderByChild("driverId").equalTo(driverId)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val allSchedules = snapshot.children.mapNotNull {
                        it.getValue(ScheduleModel::class.java)
                    }

                    // Filter logic:
                    // 1. Must be today
                    // 2. The shift must NOT have ended yet (Current Time < End Time)
                    val validSchedule = allSchedules
                        .filter { it.dayOfWeek.equals(todayName, ignoreCase = true) }
                        .filter { isShiftStillValid(currentTime, it.endTime) }
                        .minByOrNull { it.startTime } // Get the earliest upcoming one

                    callback(validSchedule)
                }

                override fun onCancelled(error: DatabaseError) { callback(null) }
            })
    }

    // Helper to compare "HH:mm" strings
    private fun isShiftStillValid(currentTime: String, endTime: String): Boolean {
        return try {
            currentTime <= endTime
        } catch (e: Exception) {
            false
        }
    }

    private fun notifyScheduleChange(
        schedule: ScheduleModel,
        type: String,
        title: String,
        message: String
    ) {
        val metadata = mapOf(
            "scheduleId" to schedule.scheduleId,
            "routeId" to schedule.routeId
        )
        if (schedule.driverId.isNotBlank()) {
            notificationRepo.sendNotification(
                recipientId = schedule.driverId,
                recipientRole = "DRIVER",
                title = title,
                message = message,
                type = type,
                metadata = metadata
            )
        }
        userRepo.getUsersByRoute(schedule.routeId) { success, _, users ->
            if (success && users != null) {
                notificationRepo.sendToUsers(
                    users.map { it.userId },
                    "USER",
                    title,
                    message,
                    type,
                    metadata
                )
            }
        }
    }
}
