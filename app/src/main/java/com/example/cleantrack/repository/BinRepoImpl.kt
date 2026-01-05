package com.example.cleantrack.repository

import com.example.cleantrack.model.BinCollectionModel
import com.example.cleantrack.model.BinModel
import com.google.firebase.database.*

class BinRepoImpl : BinRepo {

    private val ref: DatabaseReference =
        FirebaseDatabase.getInstance().getReference("Bins")
    private val refBinCollection: DatabaseReference =
        FirebaseDatabase.getInstance().getReference("BinCollections")

    fun addBinCollection(model: BinCollectionModel, callback: (Boolean, String) -> Unit) {
        val collectionId = refBinCollection.push().key ?: ""
        model.collectionId = collectionId

        refBinCollection.child(collectionId)
            .setValue(model)
            .addOnCompleteListener {
                if (it.isSuccessful) callback(true, "Bin collection saved")
                else callback(false, it.exception?.message ?: "Error")
            }
    }

    override fun addBin(model: BinModel, callback: (Boolean, String) -> Unit) {
        val key = ref.push().key ?: ""
        model.binId = key
        model.qrValue = "BIN:$key"

        ref.child(key)
            .setValue(model)
            .addOnCompleteListener {
                if (it.isSuccessful) callback(true, "Bin added")
                else callback(false, it.exception?.message ?: "Error")
            }
    }

    override fun updateBin(model: BinModel, callback: (Boolean, String) -> Unit) {
        ref.child(model.binId)
            .setValue(model)
            .addOnCompleteListener {
                if (it.isSuccessful) callback(true, "Bin updated")
                else callback(false, it.exception?.message ?: "Error")
            }
    }

    override fun deleteBin(binId: String, callback: (Boolean, String) -> Unit) {
        ref.child(binId)
            .removeValue()
            .addOnCompleteListener {
                if (it.isSuccessful) callback(true, "Bin deleted")
                else callback(false, it.exception?.message ?: "Error")
            }
    }

    override fun getBinsByUser(
        userId: String,
        callback: (Boolean, String, List<BinModel>?) -> Unit
    ) {
        ref.orderByChild("ownerUserId")
            .equalTo(userId)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val list = mutableListOf<BinModel>()
                    snapshot.children.forEach {
                        it.getValue(BinModel::class.java)?.let(list::add)
                    }
                    callback(true, "Fetched", list)
                }

                override fun onCancelled(error: DatabaseError) {
                    callback(false, error.message, null)
                }
            })
    }

    override fun getBinById(
        binId: String,
        callback: (Boolean, String, BinModel?) -> Unit
    ) {
        ref.child(binId)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    callback(
                        snapshot.exists(),
                        if (snapshot.exists()) "Fetched" else "Not found",
                        snapshot.getValue(BinModel::class.java)
                    )
                }

                override fun onCancelled(error: DatabaseError) {
                    callback(false, error.message, null)
                }
            })
    }
}
