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

    // ADD THIS: Use this for QR Scanning/Validation (No Toast Loop)
    override fun getCollectionsByTripOnce(
        tripId: String,
        callback: (Boolean, String, List<BinCollectionModel>?) -> Unit
    ) {
        ref.orderByChild("tripId").equalTo(tripId).get()
            .addOnSuccessListener { snapshot ->
                val list = snapshot.children.mapNotNull { it.getValue(BinCollectionModel::class.java) }
                callback(true, "Data fetched", list)
            }
            .addOnFailureListener {
                callback(false, it.message ?: "Fetch failed", null)
            }
    }

    override fun getLatestCollectionForUser(
        userId: String,
        callback: (Boolean, String?, BinCollectionModel?) -> Unit
    ) {
        // Query collections where "userId" matches, ordered by timestamp
        ref.orderByChild("userId").equalTo(userId)
            .limitToLast(1) // We only want the most recent one
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        // Firebase returns a map even for one item when using equalTo
                        val collection = snapshot.children.first().getValue(BinCollectionModel::class.java)
                        callback(true, "Success", collection)
                    } else {
                        callback(false, "No collections found", null)
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    callback(false, error.message, null)
                }
            })
    }
}
