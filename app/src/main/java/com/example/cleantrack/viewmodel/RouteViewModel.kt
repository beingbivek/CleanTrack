package com.example.cleantrack.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cleantrack.model.RouteAssignmentModel
import com.example.cleantrack.model.RouteModel
import com.example.cleantrack.repository.RouteRepo
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class RouteViewModel(
    private val repo: RouteRepo
) : ViewModel() {

    private val _routes = MutableStateFlow<List<RouteModel>>(emptyList())
    val routes: StateFlow<List<RouteModel>> = _routes

    private val _saving = MutableStateFlow(false)
    val saving: StateFlow<Boolean> = _saving

    fun startRoutesListener() {
        viewModelScope.launch {
            repo.observeRoutes().collect { _routes.value = it }
        }
    }

    fun saveNewRoute(route: RouteModel, onDone: (Boolean, String?) -> Unit) {
        viewModelScope.launch {
            _saving.value = true
            val res = repo.createRoute(route)
            _saving.value = false
            onDone(res.isSuccess, res.exceptionOrNull()?.message)
        }
    }

    fun assignRoute(a: RouteAssignmentModel, onDone: (Boolean, String?) -> Unit) {
        viewModelScope.launch {
            val res = repo.createAssignment(a)
            onDone(res.isSuccess, res.exceptionOrNull()?.message)
        }
    }
}
