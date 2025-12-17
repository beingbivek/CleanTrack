package com.example.cleantrack.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cleantrack.model.map.RouteAssignmentModel
import com.example.cleantrack.model.map.RouteModel
import com.example.cleantrack.repository.RouteRepo
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class RouteViewModel(private val repo: RouteRepo) : ViewModel() {

    private val _routes = MutableStateFlow<List<RouteModel>>(emptyList())
    val routes: StateFlow<List<RouteModel>> = _routes

    fun startRoutesListener() {
        viewModelScope.launch {
            repo.observeRoutes().collect { _routes.value = it }
        }
    }

    fun saveNewRoute(route: RouteModel, onDone: (Boolean, String?) -> Unit) {
        viewModelScope.launch {
            val res = repo.createRoute(route)
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
