package com.example.cleantrack.repository

import com.example.cleantrack.model.map.RouteAssignmentModel
import com.example.cleantrack.model.map.RouteModel
import com.google.firebase.database.*
import com.google.firebase.database.ktx.getValue
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
class RouteRepoImpl : RouteRepo {

    private val ref =
        FirebaseDatabase.getInstance().getReference("Routes")

    override fun addRoute(model: RouteModel, callback: (Boolean, String) -> Unit) {
        model.routeId = ref.push().key ?: ""
        ref.child(model.routeId).setValue(model).addOnCompleteListener {
            if (it.isSuccessful)
                callback(true, "Route added successfully")
            else
                callback(false, it.exception?.message ?: "Failed")
        }
    }

    override fun updateRoute(model: RouteModel, callback: (Boolean, String) -> Unit) {
        ref.child(model.routeId).setValue(model).addOnCompleteListener {
            if (it.isSuccessful)
                callback(true, "Route updated successfully")
            else
                callback(false, it.exception?.message ?: "Failed")
        }
    }

    override fun deleteRoute(routeId: String, callback: (Boolean, String) -> Unit) {
        ref.child(routeId).removeValue().addOnCompleteListener {
            if (it.isSuccessful)
                callback(true, "Route deleted")
            else
                callback(false, it.exception?.message ?: "Failed")
        }
    }

    override fun getAllRoutes(callback: (Boolean, String, List<RouteModel>?) -> Unit) {
        ref.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val routes = mutableListOf<RouteModel>()
                snapshot.children.forEach {
                    it.getValue(RouteModel::class.java)?.let(routes::add)
                }
                callback(true, "Routes fetched", routes)
            }

            override fun onCancelled(error: DatabaseError) {
                callback(false, error.message, null)
            }
        })
    }

    override fun getRouteById(
        routeId: String,
        callback: (Boolean, String, RouteModel?) -> Unit
    ) {
        ref.child(routeId)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    callback(
                        true,
                        "Route fetched",
                        snapshot.getValue(RouteModel::class.java)
                    )
                }

                override fun onCancelled(error: DatabaseError) {
                    callback(false, error.message, null)
                }
            })
    }
}
