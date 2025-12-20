package com.example.cleantrack.repository

import com.example.cleantrack.model.ScheduleModel
import com.google.firebase.database.*

class ScheduleRepoImpl : ScheduleRepo {

    private val database: FirebaseDatabase = FirebaseDatabase.getInstance()
    private val ref: DatabaseReference = database.getReference("Schedules")

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
        ref.child(scheduleId)
            .removeValue()
            .addOnCompleteListener {
                if (it.isSuccessful) {
                    callback(true, "Schedule deleted successfully")
                } else {
                    callback(false, it.exception?.message ?: "Error")
                }
            }
    }
}
