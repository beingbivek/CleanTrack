package com.example.cleantrack.repository

import com.example.cleantrack.model.map.RouteAssignmentModel
import com.example.cleantrack.model.map.RouteModel
import kotlinx.coroutines.flow.Flow

interface RouteRepo {
    suspend fun createRoute(route: RouteModel): Result<String>
    suspend fun updateRoute(route: RouteModel): Result<Unit>
    suspend fun deleteRoute(routeId: String): Result<Unit>

    fun observeRoutes(): Flow<List<RouteModel>>

    suspend fun createAssignment(a: RouteAssignmentModel): Result<String>
    fun observeAssignmentsForDriver(driverId: String): Flow<List<RouteAssignmentModel>>
}
