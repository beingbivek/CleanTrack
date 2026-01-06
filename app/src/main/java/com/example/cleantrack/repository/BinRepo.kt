package com.example.cleantrack.repository

import com.example.cleantrack.model.BinModel

interface BinRepo {

    fun addBin(
        model: BinModel,
        callback: (Boolean, String) -> Unit
    )

    fun updateBin(
        model: BinModel,
        callback: (Boolean, String) -> Unit
    )

    fun deleteBin(
        binId: String,
        callback: (Boolean, String) -> Unit
    )

    fun getBinsByUser(
        userId: String,
        callback: (Boolean, String, List<BinModel>?) -> Unit
    )

    fun getBinById(
        binId: String,
        callback: (Boolean, String, BinModel?) -> Unit
    )

    // Gets all bins belonging to a list of user IDs
    fun getBinsByOwnerIds(ownerIds: List<String>, callback: (List<BinModel>) -> Unit)
}
