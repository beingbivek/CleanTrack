package com.example.cleantrack.viewmodel

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.example.cleantrack.model.PointsTransactionModel
import com.example.cleantrack.repository.PointsRepo
import com.example.cleantrack.repository.PointsRepoImpl

class PointsHistoryViewModel(
    private val repo: PointsRepo = PointsRepoImpl()
) : ViewModel() {

    private val _history = mutableStateOf<List<PointsTransactionModel>>(emptyList())
    val history: State<List<PointsTransactionModel>> = _history

    private val _isLoading = mutableStateOf(true)
    val isLoading: State<Boolean> = _isLoading

    fun loadHistory(userId: String) {
        _isLoading.value = true
        repo.getPointsHistory(userId) { data ->
            _history.value = data
            _isLoading.value = false
        }
    }
}

