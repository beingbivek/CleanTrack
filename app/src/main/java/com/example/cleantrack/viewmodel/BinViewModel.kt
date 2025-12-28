package com.example.cleantrack.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.cleantrack.model.BinModel
import com.example.cleantrack.repository.BinRepo

class BinViewModel(private val repo: BinRepo) : ViewModel() {

    val bins = MutableLiveData<List<BinModel>>(emptyList())
    val bin = MutableLiveData<BinModel?>(null)
    val loading = MutableLiveData(false)

    fun loadUserBins(userId: String) {
        loading.postValue(true)
        repo.getBinsByUser(userId) { ok, _, data ->
            if (ok) bins.postValue(data ?: emptyList())
            loading.postValue(false)
        }
    }

    fun getBinById(binId: String) {
        loading.postValue(true)
        repo.getBinById(binId) { ok, _, data ->
            if (ok) bin.postValue(data)
            loading.postValue(false)
        }
    }

    fun addBin(model: BinModel, cb: (Boolean, String) -> Unit) =
        repo.addBin(model, cb)

    fun updateBin(model: BinModel, cb: (Boolean, String) -> Unit) =
        repo.updateBin(model, cb)

    fun deleteBin(binId: String, cb: (Boolean, String) -> Unit) =
        repo.deleteBin(binId, cb)
}
