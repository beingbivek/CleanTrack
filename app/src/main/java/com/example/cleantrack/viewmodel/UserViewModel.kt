package com.example.cleantrack.viewmodel

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cleantrack.model.UserModel
import com.example.cleantrack.repository.AIRepository
import com.example.cleantrack.repository.BinCollectionRepo
import com.example.cleantrack.repository.UserRepo
import com.example.cleantrack.util.AppUtil
import com.example.cleantrack.view.admin.AdminDashboardActivity
import com.example.cleantrack.view.auth.LoginActivity
import com.example.cleantrack.view.auth.RegistrationActivity
import com.example.cleantrack.view.auth.UserLocationMapActivity

import com.example.cleantrack.view.driver.DriverDashboardActivity
import com.example.cleantrack.view.user.UserDashboardActivity
import kotlinx.coroutines.launch


class UserViewModel(
    val repo : UserRepo,
    val collectionRepo: BinCollectionRepo = com.example.cleantrack.repository.BinCollectionRepoImpl()
) : ViewModel() {

    private val _latestCollection = MutableLiveData<com.example.cleantrack.model.BinCollectionModel?>()
    val latestCollection: androidx.lifecycle.LiveData<com.example.cleantrack.model.BinCollectionModel?>
        get() = _latestCollection
    private val _globalAiReview = MutableLiveData<String>("Gathering your history...")
    val globalAiReview: LiveData<String> = _globalAiReview

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

    // Inside UserViewModel.kt

    private val _userPoints = MutableLiveData<Int>()
    val userPoints: MutableLiveData<Int> get() = _userPoints

    fun awardPoints(userId: String, points: Int) {
        repo.addUserPoints(userId, points) { success, message ->
            if (success) {
                Log.d("UserVM", "Points updated successfully for $userId")
                // Refresh local points value
                fetchUserPoints(userId)
            } else {
                Log.e("UserVM", "Failed to update points: $message")
            }
        }
    }

    fun fetchUserPoints(userId: String) {
        repo.getUserPoints(userId) { success, _, points ->
            if (success) _userPoints.postValue(points)
        }
    }

    fun fetchLatestAIReview(userId: String) {
        collectionRepo.getLatestCollectionForUser(userId) { success, _, collection ->
            if (success) {
                _latestCollection.postValue(collection)
            }
        }
    }

    // --- Add these inside UserViewModel class ---

    private val _sellerData = MutableLiveData<UserModel?>()
    val sellerData: MutableLiveData<UserModel?> get() = _sellerData

    private val _highestBidderData = MutableLiveData<UserModel?>()
    val highestBidderData: MutableLiveData<UserModel?> get() = _highestBidderData

    // Function to specifically fetch Seller details
    fun getSellerInfo(sellerId: String) {
        repo.getUserById(sellerId) { success, _, data ->
            if (success) _sellerData.postValue(data)
        }
    }

    // Function to specifically fetch current Highest Bidder details
    fun getHighestBidderInfo(bidderId: String) {
        if (bidderId.isEmpty()) return
        repo.getUserById(bidderId) { success, _, data ->
            if (success) _highestBidderData.postValue(data)
        }
    }

    fun fetchGlobalAIReview(userId: String, aiRepo: AIRepository) {
        collectionRepo.getAllCollectionsForUser(userId) { success, _, collections ->
            if (success && !collections.isNullOrEmpty()) {
                // We use viewModelScope to run the heavy AI call off the main thread
                viewModelScope.launch {
                    val review = aiRepo.generateGlobalOverview(collections)
                    _globalAiReview.postValue(review)
                }
            } else {
                _globalAiReview.postValue("Start disposing waste to see your personalized AI tips!")
            }
        }
    }


    /**
     * Logic to check if the user has an active premium subscription.
     * It checks the boolean flag and validates that the expiry date hasn't passed.
     */
    fun isPremiumUser(user: UserModel?): Boolean {
        if (user == null) return false

        val currentTime = System.currentTimeMillis()

        // Check root flag
        val hasFlag = user.subscription?.isSubscribed

        // Check root expiry OR nested expiry
        val expiry = user.subscription?.expiryDate ?: 0L

        // Check if the subscription object itself exists (backup check)
        val hasSubscriptionObject = user.subscription != null

        // It is premium if (Flag is true OR Subscription object exists) AND it's not expired
        return (hasFlag == true || hasSubscriptionObject) && expiry > currentTime
    }

    /**
     * Returns a user-friendly string of days remaining
     */
    fun getSubscriptionDaysRemaining(user: UserModel?): Long {
        val expiry = user?.subscription?.expiryDate ?: return 0L
        val diff = expiry - System.currentTimeMillis()
        return if (diff > 0) diff / (24 * 60 * 60 * 1000) else 0L
    }

    // Add a check in your existing getUserById to refresh the local _user state
    fun refreshUser(userId: String) {
        repo.getUserById(userId) { success, _, data ->
            if (success) _user.postValue(data)
        }
    }


}