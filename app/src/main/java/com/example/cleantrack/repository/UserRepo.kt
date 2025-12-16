package com.example.cleantrack.repository

import com.example.cleantrack.model.UserModel

interface UserRepo {

    fun login(email : String , password : String , callback : (Boolean, String?, String?,String?)-> Unit)

    fun signInWithGoogle(idToken: String, callback: (Boolean, String?, String?) -> Unit)

    fun register (email: String, password: String, callback: (Boolean, String, String) -> Unit)

    fun addUserToDatabase(userId : String, model : UserModel, callback: (Boolean, String) -> Unit)

    fun forgotPassword(email: String, callback: (Boolean, String) -> Unit)

    fun getUserById(userId: String, callback: (Boolean, String, UserModel?) -> Unit)

    fun getAllUsers(callback: (Boolean, String, List<UserModel>) -> Unit)

    fun editUserProfile(userId: String, model: UserModel, callback: (Boolean, String) -> Unit)

    fun deleteUser(userId: String, callback: (Boolean, String) -> Unit)

    fun saveUserLocation(userId: String, latitude : Double, longitude : Double, callback: (Boolean, String) -> Unit)
}