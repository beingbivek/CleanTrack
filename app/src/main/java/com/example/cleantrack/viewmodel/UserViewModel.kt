package com.example.cleantrack.viewmodel

import androidx.lifecycle.ViewModel
import com.example.cleantrack.model.UserModel
import com.example.cleantrack.repository.UserRepo

class UserViewModel(val repo : UserRepo) : ViewModel() {

    fun login(email : String , password : String , callback : (Boolean, String?, String?)-> Unit){
                repo.login(email, password, callback)
    }

    fun signInWithGoogle(idToken: String, callback: (Boolean, String?, String?) -> Unit){
        repo.signInWithGoogle(idToken , callback)
    }


    fun register (email: String, password: String, callback: (Boolean, String, String) -> Unit){
                repo.register(email, password, callback)
    }

    fun addUserToDatabase(userId : String, model : UserModel, callback: (Boolean, String) -> Unit){
                repo.addUserToDatabase(userId , model, callback)
    }

    fun forgotPassword(email: String, callback: (Boolean, String) -> Unit){
        repo.forgotPassword(email, callback)

    }

    fun getUserById(userId: String, callback: (Boolean, String, UserModel?) -> Unit){

        repo.getUserById(userId, callback)

    }

    fun getAllUsers(callback: (Boolean, String, List<UserModel>) -> Unit){

        repo.getAllUsers(callback)

    }

    fun editUserProfile(userId: String, model: UserModel, callback: (Boolean, String) -> Unit){

        repo.editUserProfile(userId, model, callback)

    }

    fun deleteUser(userId: String, callback: (Boolean, String) -> Unit){

        repo.deleteUser(userId, callback)

    }
}