package com.example.cleantrack.repository

import com.example.cleantrack.model.BinCollectionModel
import com.google.firebase.database.FirebaseDatabase

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
}
