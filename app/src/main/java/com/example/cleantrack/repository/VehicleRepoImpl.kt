package com.example.cleantrack.repository

import com.example.cleantrack.model.VehicleModel
import com.google.firebase.database.*

class VehicleRepoImpl : VehicleRepo {

    private val ref = FirebaseDatabase.getInstance().getReference("Vehicles")

    override fun addVehicle(model: VehicleModel, callback: (Boolean, String) -> Unit) {
        model.vehicleId = ref.push().key ?: ""
        ref.child(model.vehicleId)
            .setValue(model)
            .addOnCompleteListener {
                if (it.isSuccessful) callback(true, "Vehicle added")
                else callback(false, it.exception?.message ?: "Error")
            }
    }

    override fun getAllVehicles(callback: (Boolean, String, List<VehicleModel>) -> Unit) {
        ref.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val list = mutableListOf<VehicleModel>()
                snapshot.children.forEach {
                    it.getValue(VehicleModel::class.java)?.let(list::add)
                }
                callback(true, "Vehicles loaded", list)
            }

            override fun onCancelled(error: DatabaseError) {
                callback(false, error.message, emptyList())
            }
        })
    }

    override fun getVehicleById(
        vehicleId: String,
        callback: (Boolean, String, VehicleModel?) -> Unit
    ) {
        ref.child(vehicleId)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    callback(
                        snapshot.exists(),
                        if (snapshot.exists()) "Vehicle loaded" else "Not found",
                        snapshot.getValue(VehicleModel::class.java)
                    )
                }

                override fun onCancelled(error: DatabaseError) {
                    callback(false, error.message, null)
                }
            })
    }

    override fun updateVehicle(model: VehicleModel, callback: (Boolean, String) -> Unit) {
        ref.child(model.vehicleId)
            .setValue(model)
            .addOnCompleteListener {
                if (it.isSuccessful) callback(true, "Vehicle updated")
                else callback(false, it.exception?.message ?: "Error")
            }
    }

    override fun deleteVehicle(vehicleId: String, callback: (Boolean, String) -> Unit) {
        ref.child(vehicleId)
            .removeValue()
            .addOnCompleteListener {
                if (it.isSuccessful) callback(true, "Vehicle deleted")
                else callback(false, it.exception?.message ?: "Error")
            }
    }
}
