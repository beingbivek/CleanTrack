package com.example.cleantrack.repository

import android.util.Log
import com.example.cleantrack.model.schedule.ScheduleModel
import com.google.firebase.database.*

class ScheduleRepoImpl : ScheduleRepo {

    private val database = FirebaseDatabase.getInstance()
    private val ref: DatabaseReference = database.getReference("Schedules")

    override fun createSchedule(
        model: ScheduleModel,
        callback: (Boolean, String) -> Unit
    ) {
        val scheduleId = ref.push().key ?: return callback(false, "Failed to create schedule ID")

        val finalModel = model.copy(
            scheduleId = scheduleId,
            createdAt = System.currentTimeMillis()
        )

        ref.child(scheduleId).setValue(finalModel)
            .addOnSuccessListener {
                callback(true, "Schedule created")
            }
            .addOnFailureListener {
                callback(false, it.localizedMessage ?: "Failed to create schedule")
            }
    }

    override fun getAllSchedules(
        callback: (Boolean, String, List<ScheduleModel>) -> Unit
    ) {
        ref.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val list = mutableListOf<ScheduleModel>()
                for (child in snapshot.children) {
                    val schedule = child.getValue(ScheduleModel::class.java)
                    if (schedule != null && schedule.isActive) {
                        list.add(schedule)
                    }
                }
                callback(true, "Schedules fetched", list)
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("ScheduleRepo", error.message)
                callback(false, error.message, emptyList())
            }
        })
    }

    override fun getSchedulesForDriver(
        driverId: String,
        callback: (Boolean, String, List<ScheduleModel>) -> Unit
    ) {
        ref.orderByChild("driverId").equalTo(driverId)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val list = snapshot.children.mapNotNull {
                        it.getValue(ScheduleModel::class.java)
                    }.filter { it.isActive }

                    callback(true, "Driver schedules fetched", list)
                }

                override fun onCancelled(error: DatabaseError) {
                    callback(false, error.message, emptyList())
                }
            })
    }

    override fun updateSchedule(
        scheduleId: String,
        model: ScheduleModel,
        callback: (Boolean, String) -> Unit
    ) {
        ref.child(scheduleId).updateChildren(model.toMap())
            .addOnCompleteListener {
                if (it.isSuccessful) callback(true, "Schedule updated")
                else callback(false, it.exception?.message ?: "Update failed")
            }
    }

    override fun deactivateSchedule(
        scheduleId: String,
        callback: (Boolean, String) -> Unit
    ) {
        ref.child(scheduleId).child("isActive").setValue(false)
            .addOnSuccessListener {
                callback(true, "Schedule deactivated")
            }
            .addOnFailureListener {
                callback(false, it.localizedMessage ?: "Failed to deactivate")
            }
    }
}
