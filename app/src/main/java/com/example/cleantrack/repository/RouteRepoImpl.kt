package com.example.cleantrack.repository

import com.example.cleantrack.model.map.RouteAssignmentModel
import com.example.cleantrack.model.map.RouteModel
import com.google.firebase.database.*
import com.google.firebase.database.ktx.getValue
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class RouteRepoImpl(
    private val db: FirebaseDatabase = FirebaseDatabase.getInstance()
) : RouteRepo {

    // Recommended structure:
    // routes/{routeId}
    // routeAssignments/{assignmentId}

    private val routesRef = db.getReference("routes")
    private val assignmentsRef = db.getReference("routeAssignments")

    override suspend fun createRoute(route: RouteModel): Result<String> = runCatching {
        val id = routesRef.push().key ?: throw IllegalStateException("Failed to create key")
        val final = route.copy(
            routeId = id,
            createdAt = System.currentTimeMillis()
        )
        routesRef.child(id).setValue(final).await()
        id
    }

    override suspend fun updateRoute(route: RouteModel): Result<Unit> {
        TODO("Not yet implemented")
    }

    override suspend fun deleteRoute(routeId: String): Result<Unit> {
        TODO("Not yet implemented")
    }

    override fun observeRoutes(): Flow<List<RouteModel>> = callbackFlow {
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val list = snapshot.children.mapNotNull { it.getValue<RouteModel>() }
                    .filter { it.isActive }
                trySend(list)
            }

            override fun onCancelled(error: DatabaseError) {
                close(error.toException())
            }
        }

        routesRef.addValueEventListener(listener)
        awaitClose { routesRef.removeEventListener(listener) }
    }

    override suspend fun createAssignment(a: RouteAssignmentModel): Result<String> = runCatching {
        val id = assignmentsRef.push().key ?: throw IllegalStateException("Failed to create key")
        val final = a.copy(
            assignmentId = id,
            createdAt = System.currentTimeMillis()
        )
        assignmentsRef.child(id).setValue(final).await()
        id
    }

    override fun observeAssignmentsForDriver(driverId: String): Flow<List<RouteAssignmentModel>> = callbackFlow {
        // Query: routeAssignments where driverId == driverId
        val query = assignmentsRef.orderByChild("driverId").equalTo(driverId)

        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val list = snapshot.children.mapNotNull { it.getValue<RouteAssignmentModel>() }
                    .filter { it.isActive }
                trySend(list)
            }

            override fun onCancelled(error: DatabaseError) {
                close(error.toException())
            }
        }

        query.addValueEventListener(listener)
        awaitClose { query.removeEventListener(listener) }
    }
}
