package com.example.cleantrack.repository

import android.util.Log
import com.example.cleantrack.model.VehicleModel
import com.google.firebase.database.*

class VehicleRepoImpl : VehicleRepo {

    private val database = FirebaseDatabase.getInstance()
    private val ref: DatabaseReference = database.getReference("Vehicles")

    override fun addVehicle(model: VehicleModel, callback: (Boolean, String) -> Unit) {
        val id = ref.push().key ?: return callback(false, "Failed to create vehicle ID")

        val finalModel = model.copy(vehicleId = id)

        ref.child(id).setValue(finalModel)
            .addOnSuccessListener {
                callback(true, "Vehicle added")
            }
            .addOnFailureListener {
                callback(false, it.localizedMessage ?: "Failed to add vehicle")
            }
    }

    override fun getAllVehicles(callback: (Boolean, String, List<VehicleModel>) -> Unit) {
        ref.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val list = mutableListOf<VehicleModel>()
                for (child in snapshot.children) {
                    val v = child.getValue(VehicleModel::class.java)
                    if (v != null && v.isActive) list.add(v)
                }
                callback(true, "Vehicles fetched", list)
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("VehicleRepo", error.message)
                callback(false, error.message, emptyList())
            }
        })
    }

    override fun deactivateVehicle(vehicleId: String, callback: (Boolean, String) -> Unit) {
        ref.child(vehicleId).child("isActive").setValue(false)
            .addOnSuccessListener { callback(true, "Vehicle deactivated") }
            .addOnFailureListener {
                callback(false, it.localizedMessage ?: "Failed to deactivate")
            }
    }
}
