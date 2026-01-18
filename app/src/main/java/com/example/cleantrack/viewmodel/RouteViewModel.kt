package com.example.cleantrack.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cleantrack.model.map.RouteAssignmentModel
import com.example.cleantrack.model.map.RouteModel
import com.example.cleantrack.repository.RouteRepo
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class RouteViewModel(
    private val repo: RouteRepo
) : ViewModel() {

    val routes = MutableLiveData<List<RouteModel>>()
    val route = MutableLiveData<RouteModel?>()
    val loading = MutableLiveData(false)

    fun loadRoutes() {
        loading.postValue(true)
        repo.getAllRoutes { success, _, data ->
            if (success) routes.postValue(data ?: emptyList())
            loading.postValue(false)
        }
    }

    fun getRouteById(routeId: String) {
        repo.getRouteById(routeId) { success, _, data ->
            if (success) route.postValue(data)
        }
    }

    fun addRoute(model: RouteModel, cb: (Boolean, String) -> Unit) =
        repo.addRoute(model, cb)

    fun updateRoute(model: RouteModel, cb: (Boolean, String) -> Unit) =
        repo.updateRoute(model, cb)

    fun deleteRoute(routeId: String, cb: (Boolean, String) -> Unit) =
        repo.deleteRoute(routeId, cb)
}

