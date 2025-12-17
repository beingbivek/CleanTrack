package com.example.cleantrack.repository

import com.example.cleantrack.model.ScheduleModel
import com.google.firebase.database.*

class ScheduleRepoImpl : ScheduleRepo {

    private val ref = FirebaseDatabase
        .getInstance()
        .getReference("Schedules")

    override fun addSchedule(model: ScheduleModel, callback: (Boolean, String) -> Unit) {
        val id = ref.push().key ?: return callback(false, "Failed to create schedule")

        val finalModel = model.copy(scheduleId = id)

        ref.child(id).setValue(finalModel)
            .addOnSuccessListener { callback(true, "Schedule added") }
            .addOnFailureListener { callback(false, it.localizedMessage ?: "Failed") }
    }

    override fun getSchedulesForDriver(
        driverId: String,
        callback: (Boolean, String, List<ScheduleModel>) -> Unit
    ) {
        ref.orderByChild("driverId").equalTo(driverId)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val list = snapshot.children
                        .mapNotNull { it.getValue(ScheduleModel::class.java) }
                        .filter { it.active }

                    callback(true, "Fetched", list)
                }

                override fun onCancelled(error: DatabaseError) {
                    callback(false, error.message, emptyList())
                }
            })
    }

    override fun getAllSchedules(
        callback: (Boolean, String, List<ScheduleModel>) -> Unit
    ) {
        ref.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val list = snapshot.children
                    .mapNotNull { it.getValue(ScheduleModel::class.java) }

                callback(true, "Fetched", list)
            }

            override fun onCancelled(error: DatabaseError) {
                callback(false, error.message, emptyList())
            }
        })
    }

    override fun deleteSchedule(scheduleId: String, callback: (Boolean, String) -> Unit) {
        ref.child(scheduleId).removeValue()
            .addOnSuccessListener { callback(true, "Schedule removed") }
            .addOnFailureListener { callback(false, it.localizedMessage ?: "Failed") }
    }
}
