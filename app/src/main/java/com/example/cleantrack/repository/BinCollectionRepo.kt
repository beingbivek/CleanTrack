package com.example.cleantrack.repository

import com.example.cleantrack.model.BinCollectionModel

interface BinCollectionRepo {

    fun addBinCollection(
        model: BinCollectionModel,
        callback: (Boolean, String) -> Unit
    )
}