package com.example.cleantrack.repository

import com.example.cleantrack.model.map.RouteAssignmentModel
import com.example.cleantrack.model.map.RouteModel
import kotlinx.coroutines.flow.Flow

interface RouteRepo {

    fun addRoute(
        model: RouteModel,
        callback: (Boolean, String) -> Unit
    )

    fun updateRoute(
        model: RouteModel,
        callback: (Boolean, String) -> Unit
    )

    fun deleteRoute(
        routeId: String,
        callback: (Boolean, String) -> Unit
    )

    fun getAllRoutes(
        callback: (Boolean, String, List<RouteModel>?) -> Unit
    )

    fun getRouteById(
        routeId: String,
        callback: (Boolean, String, RouteModel?) -> Unit
    )
}
