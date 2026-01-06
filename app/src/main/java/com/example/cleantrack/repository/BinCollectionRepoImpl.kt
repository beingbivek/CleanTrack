package com.example.cleantrack.repository

import com.example.cleantrack.model.BinCollectionModel
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class BinCollectionRepoImpl : BinCollectionRepo {

    private val ref =
        FirebaseDatabase.getInstance().getReference("BinCollections")

    override fun addBinCollection(
        model: BinCollectionModel,
        callback: (Boolean, String) -> Unit
    ) {
        val key = ref.push().key ?: ""
        model.collectionId = key

        ref.child(key)
            .setValue(model)
            .addOnCompleteListener {
                if (it.isSuccessful) callback(true, "Collection saved")
                else callback(false, it.exception?.message ?: "Error")
            }
    }

    override fun observeCollectionsByTrip(
        tripId: String,
        callback: (Boolean, String, List<BinCollectionModel>?) -> Unit
    ) {
        ref.orderByChild("tripId").equalTo(tripId)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val list = snapshot.children.mapNotNull { it.getValue(BinCollectionModel::class.java) }
                    callback(true, "Success", list) // Fixed parameters
                }

                override fun onCancelled(error: DatabaseError) {
                    callback(false, error.message, null) // Fixed parameters
                }
            })
    }
}
