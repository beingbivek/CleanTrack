package com.example.cleantrack.repository

import com.example.cleantrack.model.BinCollectionModel

interface BinCollectionRepo {

    fun addBinCollection(
        model: BinCollectionModel,
        callback: (Boolean, String) -> Unit
    )
    // To observe how many bins are scanned for the CURRENT trip
    fun observeCollectionsByTrip(tripId: String, callback: (Boolean, String, List<BinCollectionModel>?) -> Unit)
}