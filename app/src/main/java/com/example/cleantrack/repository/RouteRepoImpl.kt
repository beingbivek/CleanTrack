package com.example.cleantrack.repository

import com.example.cleantrack.model.RouteAssignmentModel
import com.example.cleantrack.model.RouteModel
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.toObject
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class RouteRepoImpl(
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
) : RouteRepo {

    private val routesCol = db.collection("routes")
    private val assignCol = db.collection("routeAssignments")

    override suspend fun createRoute(route: RouteModel): Result<String> = runCatching {
        val doc = routesCol.document()
        val finalRoute = route.copy(routeId = doc.id)
        doc.set(finalRoute).await()
        doc.id
    }

    override suspend fun updateRoute(route: RouteModel): Result<Unit> = runCatching {
        routesCol.document(route.routeId).set(route).await()
    }

    override suspend fun deleteRoute(routeId: String): Result<Unit> = runCatching {
        routesCol.document(routeId).delete().await()
    }

    override fun observeRoutes(): Flow<List<RouteModel>> = callbackFlow {
        val reg = routesCol
            .whereEqualTo("isActive", true)
            .addSnapshotListener { snap, err ->
                if (err != null) {
                    close(err); return@addSnapshotListener
                }
                val list = snap?.documents?.mapNotNull { it.toObject<RouteModel>() } ?: emptyList()
                trySend(list)
            }
        awaitClose { reg.remove() }
    }

    override suspend fun createAssignment(a: RouteAssignmentModel): Result<String> = runCatching {
        val doc = assignCol.document()
        val final = a.copy(assignmentId = doc.id)
        doc.set(final).await()
        doc.id
    }

    override fun observeAssignmentsForDriver(driverId: String): Flow<List<RouteAssignmentModel>> = callbackFlow {
        val reg = assignCol
            .whereEqualTo("driverId", driverId)
            .whereEqualTo("isActive", true)
            .addSnapshotListener { snap, err ->
                if (err != null) { close(err); return@addSnapshotListener }
                val list = snap?.documents?.mapNotNull { it.toObject<RouteAssignmentModel>() } ?: emptyList()
                trySend(list)
            }
        awaitClose { reg.remove() }
    }
}
