package com.example.cleantrack.viewmodel

import android.util.Log
import androidx.lifecycle.MutableLiveData
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

//    Initializing getter and setter

    private val _user = MutableLiveData<UserModel?>()
    val user : MutableLiveData<UserModel?>
        get() = _user

    private val _allUsers = MutableLiveData<List<UserModel>?>()
    val allUsers : MutableLiveData<List<UserModel>?>
        get() = _allUsers

    private val _loading = MutableLiveData<Boolean>()
    val loading : MutableLiveData<Boolean>
        get() = _loading

    fun getUserById(userId: String){

        _loading.postValue(true)

        repo.getUserById(userId){
            success, message, data->
            if (success){
                _user.postValue(data)
                _loading.postValue(false)

            }
            _loading.postValue(false)
        }

    }

    fun getAllUsers(){

        _loading.postValue(true)

        repo.getAllUsers{
            success, message, data ->
                Log.d("checkpoint",success.toString())
            if (success){
                _allUsers.postValue(data)
                _loading.postValue(false)

            }else {
                // Log the error message to debug connection issues
                println("Error fetching users: $message")
                _allUsers.postValue(emptyList()) // Post empty list on failure
                _loading.postValue(false)
            }

        }

    }

    fun editUserProfile(userId: String, model: UserModel, callback: (Boolean, String) -> Unit){

        repo.editUserProfile(userId, model, callback)

    }

    fun deleteUser(userId: String, callback: (Boolean, String) -> Unit){

        repo.deleteUser(userId, callback)

    }
}