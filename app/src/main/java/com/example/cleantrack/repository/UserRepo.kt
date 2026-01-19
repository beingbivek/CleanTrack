package com.example.cleantrack.repository

import com.example.cleantrack.model.UserModel

interface UserRepo {

    fun login(email : String , password : String , callback : (Boolean, String?, String?,String?)-> Unit)

    fun signInWithGoogle(idToken: String, callback: (Boolean, String?, UserModel?, String?) -> Unit)

    fun register (email: String, password: String, callback: (Boolean, String, String) -> Unit)

    fun addUserToDatabase(userId : String, model : UserModel, callback: (Boolean, String) -> Unit)

    fun forgotPassword(email: String, callback: (Boolean, String) -> Unit)

    fun getUserById(userId: String, callback: (Boolean, String, UserModel?) -> Unit)

    fun getAllUsers(callback: (Boolean, String, List<UserModel>) -> Unit)

    fun editUserProfile(userId: String, model: UserModel, callback: (Boolean, String) -> Unit)

    fun deleteUser(userId: String, callback: (Boolean, String) -> Unit)

    fun saveUserLocation(userId: String, latitude : Double, longitude : Double, callback: (Boolean, String) -> Unit)

    fun getAllDrivers(
        callback: (Boolean, String, List<UserModel>?) -> Unit
    )

    fun logout()

    fun getCurrentUserId(): String?

    fun updateActiveRoute(userId: String, routeId: String, callback: (Boolean, String) -> Unit)

    // Finds all users assigned to a specific route
    fun getUsersByRoute(routeId: String, callback: (Boolean, String, List<UserModel>?) -> Unit)

    fun addUserPoints(userId: String, points: Int, callback: (Boolean, String) -> Unit)
    fun getUserPoints(userId: String, callback: (Boolean, String, Int) -> Unit)

    fun updateSubscription(
        userId: String,
        subscription: com.example.cleantrack.model.SubscriptionModel,
        callback: (Boolean, String) -> Unit
    )
}