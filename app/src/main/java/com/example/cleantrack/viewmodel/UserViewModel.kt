package com.example.cleantrack.viewmodel

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.cleantrack.model.UserModel
import com.example.cleantrack.repository.UserRepo
import com.example.cleantrack.util.AppUtil
import com.example.cleantrack.view.admin.AdminDashboardActivity
import com.example.cleantrack.view.auth.LoginActivity
import com.example.cleantrack.view.auth.RegistrationActivity
import com.example.cleantrack.view.auth.UserLocationMapActivity
import com.example.cleantrack.view.driver.DriverDashboardActivity
import com.example.cleantrack.view.user.UserDashboardActivity


class UserViewModel(val repo : UserRepo) : ViewModel() {

    fun login(email : String , password : String , callback : (Boolean, String?, String?, String?)-> Unit){
                repo.login(email, password, callback)
    }

    // In UserViewModel.kt

    // 1. Update the signInWithGoogle signature to match the new repo method.
    fun signInWithGoogle(
        idToken: String,
        context: Context,
        activity: Activity,
        callback: (Boolean, String?) -> Unit // Simplified callback for UI success/failure message
    ){
        repo.signInWithGoogle(idToken) { success, errorMessage, userModel, role ->
            if (success && userModel != null) {
                if (role != null) {
                    // SCENARIO 1: FULL LOGIN SUCCESS (Existing user with complete profile)
                    // We use checkAndNavigateAfterLogin which handles the location check
                    repo.getUserById(userModel!!.userId) { fetchSuccess, _, fetchedUser ->
                        if (fetchSuccess && fetchedUser != null) {
                            checkAndNavigateAfterLogin(fetchedUser.userId, context, activity)
                        } else {
                            callback(false, "Login successful but failed to fetch user data for navigation.")
                        }
                    }
                } else if (userModel != null) {
                    // SCENARIO 2: REGISTRATION/PROFILE UPDATE NEEDED (New or incomplete user)

                    val intent = Intent(context, RegistrationActivity::class.java).apply {
                        putExtra("Google_UserModel", userModel)
                    }
                    context.startActivity(intent)
                    activity.finish()
                    callback(true, "Please complete your profile.")

                } else {
                    callback(false, "Google sign-in succeeded but the next step is unclear.")
                }
            } else {
                // SCENARIO 3: SIGN-IN FAILED
                callback(false, errorMessage ?: "Google Sign-In failed.")
            }
        }
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

    private val _drivers = MutableLiveData<List<UserModel>?>()
    val drivers: MutableLiveData<List<UserModel>?>
        get() = _drivers

    fun getAllDrivers() {
        repo.getAllDrivers { success, _, data ->
            if (success) {
                _drivers.postValue(data)
            }
        }
    }

    fun getAllUsers(){

//        _loading.postValue(true)

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

    fun navigateToDashboardByRole(
        userModel: UserModel,
        context: Context,
        activity: Activity
    ) {
        val destinationActivity = when (userModel.role) {
            "ADMIN" -> AdminDashboardActivity::class.java
            "DRIVER" -> DriverDashboardActivity::class.java
            "USER" -> UserDashboardActivity::class.java
            else -> UserDashboardActivity::class.java
        }

        val intent = Intent(context, destinationActivity).apply {
            // Essential: Clear the back stack to prevent users from returning to Login/Map
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        context.startActivity(intent)
        activity.finish()
    }


    fun saveLocationAndFinalNavigate(
        userId: String,
        latitude: Double,
        longitude: Double,
        isNewRegistration: Boolean, // <-- FLAG to differentiate the two flows
        context: Context,
        activity: Activity,
        callback: (Boolean, String) -> Unit
    ) {
        // 1. Save the location (Fixing the callback syntax issue here)
        // NOTE: This assumes your repo.saveUserLocation callback is (Boolean, String) -> Unit
        repo.saveUserLocation(userId, latitude, longitude) { success, message ->
            if (success) {

                if (isNewRegistration) {
                    // SCENARIO 1: Coming from Registration (Requirement 1)
                    val intent = Intent(context, LoginActivity::class.java)
                    context.startActivity(intent)
                    activity.finish()
                    callback(true, "Location saved. Please login to complete registration.")

                } else {
                    // SCENARIO 2: Coming from Login (Requirement 2)

                    // Fetch the full user model to get the role
                    repo.getUserById(userId) { fetchSuccess, fetchMessage, userModel ->
                        if (fetchSuccess && userModel != null) {
                            // Navigate based on the fetched role directly to the dashboard
                            navigateToDashboardByRole(userModel, context, activity)
                            callback(true, "Location saved and navigating to your dashboard.")
                        } else {
                            // Fallback
                            callback(false, "Location saved but failed to fetch user profile. Please restart the app.")
                        }
                    }
                }
            } else {
                // Location save failed
                callback(false, message)
            }
        }
    }

    fun checkAndNavigateAfterLogin(userId: String, context : Context, activity : Activity){
        repo.getUserById(userId){
            success, message, userModel ->
            if (success && userModel != null){
//                val destinationActivity: Class<*>

                // Check if location fields are null/missing
                val locationMissing = userModel.latitude == null || userModel.longitude == null

                if (locationMissing){

                    val intent = Intent(context, UserLocationMapActivity::class.java)
                        .apply {
                            putExtra("userId", userId)

                            putExtra("IS_NEW_REGISTRATION", false)
                        }

                    context.startActivity(intent)
                    activity.finish()

                }else{

                    val destinationActivity = when(userModel.role){
                        "ADMIN" -> AdminDashboardActivity::class.java
                        "DRIVER" -> DriverDashboardActivity::class.java
                        "USER" -> UserDashboardActivity::class.java
                        else -> UserDashboardActivity::class.java
                    }

                    val intent = Intent(context, destinationActivity)

                    context.startActivity(intent)

                    activity.finish()
                }

            }else{
                // Failed to fetch user data after successful login
                AppUtil.showToast(context, "Login succeeded but failed to fetch user profile: $message")
            }
        }


    }

    fun logout(){
        repo.logout()
    }

    fun getCurrentUserId(): String?{
        return repo.getCurrentUserId()
    }

    fun updateActiveRoute(userId: String, routeId: String) {
        repo.updateActiveRoute(userId, routeId) { success, message ->
            if (!success) {
                Log.e("UserVM", "Error saving route: $message")
            }
        }
    }
}