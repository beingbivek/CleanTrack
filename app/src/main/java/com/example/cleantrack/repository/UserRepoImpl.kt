package com.example.cleantrack.repository

import android.util.Log
import com.example.cleantrack.model.UserModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class UserRepoImpl : UserRepo{

    val auth : FirebaseAuth = FirebaseAuth.getInstance()

    val database : FirebaseDatabase = FirebaseDatabase.getInstance()

    val ref : DatabaseReference = database.getReference("Users")



    override fun login(
        email: String,
        password: String,
        callback: (Boolean, String?, String?, String?) -> Unit
    ) {
            auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener {
                    if (it.isSuccessful){

                        val userId = it.result?.user?.uid

                        if (userId != null){

                            val roleRef = ref.child(userId).child("role")

                            roleRef.get()
                                .addOnSuccessListener { snapshot ->
                                    val role = snapshot.getValue(String::class.java)

                                    if (role != null){
                                        callback(true, "login successfull",role, userId)
                                    }else{
                                        callback(false, "Login successful, but user role not defined.", null, null)
                                    }

                                }
                                .addOnFailureListener { e ->
                                    callback(false, "Login successful, but failed to fetch role: ${e.localizedMessage}", null, null)
                                }

                        }
                        else {
                            callback(false, "Login successful, but userID is missing.", null, null)
                        }


                    }else{
                        callback(false, "${it.exception?.message}", null, null)
                    }
                }
    }

    override fun signInWithGoogle(idToken: String, callback: (Boolean, String?,UserModel?, String?) -> Unit) {
        val credential = GoogleAuthProvider.getCredential(idToken,null)

        auth.signInWithCredential(credential)
            .addOnCompleteListener { authTask ->
                if (authTask.isSuccessful){
                    val user = authTask.result?.user
                    val userId = user?.uid

                    if (userId != null && user.email != null){

                        // checks if user document already exists in firestore

                        val roleRefr = ref.child(userId)

                        roleRefr
                            .get()
                            .addOnSuccessListener { documentSnapshot ->
                                val userModel = documentSnapshot.getValue(UserModel::class.java)


                                if(documentSnapshot.exists() && userModel != null){

                                    if (userModel.number.isNullOrEmpty()) {
                                        // User exists but mandatory field is missing -> Navigate to Registration
                                        callback(true, null, userModel, null) // success, null, userModel (for pre-fill), null
                                    } else {
                                        // User exists and registration is complete -> Proceed to Login/Dashboard
                                        callback(true, null, userModel  , userModel.role) // success, null, null, role
                                    }
                                } else  {

                                    // New user : create its firestore document
                                    val defaultRole = "USER"
                                    val userModel = UserModel(
                                        email = user.email!!,
                                        fullname = user.displayName ?: "New User",
                                        number = "",
                                        role = defaultRole,
                                        userId = userId
                                        ,
                                        // Initialize with empty strings so the keys exist in the DB
                                        province = "",
                                        district = "",
                                        municipality = "",
                                        ward = ""
                                    )

                                    ref.child(userId).setValue(userModel)
                                        .addOnSuccessListener {
                                           callback (true, null,userModel, null)
                                        }
                                        .addOnFailureListener { dbError ->
                                            callback(false, "Google sign-in succeeded, but failed to create user document : ${dbError.localizedMessage}",null, null)
                                        }
                                }
                            }
                            .addOnFailureListener { fetchError ->
                                callback(false, "Google sign-in succeeded, but failed to check user document: ${fetchError.localizedMessage}", null, null)
                            }

                    }else{
                        callback(false, "Google sign-in succeeded, but missing required user details (ID/Email).", null, null)
                    }

                }else{
                    callback(false, authTask.exception?.localizedMessage, null, null)
                }
            }
    }

    override fun register(
        email: String,
        password: String,
        callback: (Boolean, String, String) -> Unit
    ) {
        auth.createUserWithEmailAndPassword(email   , password)
            .addOnCompleteListener {
                if (it.isSuccessful){
                    callback(true, "Registration success", "${auth.currentUser?.uid}")
                }else{
                    callback(false, "${it.exception?.message}","")
                }
            }
    }

    override fun addUserToDatabase(
        userId: String,
        model: UserModel,
        callback: (Boolean, String) -> Unit
    ) {
        ref.child(userId).setValue(model)
            .addOnCompleteListener {
                if (it.isSuccessful){

                    callback(true, "Registration successful. Now confirm your location.")

                }else{
                    callback(false, "${it.exception?.message}")
                }
            }
    }

    override fun forgotPassword(
        email: String,
        callback: (Boolean, String) -> Unit
    ) {
        auth.sendPasswordResetEmail(email)
            .addOnCompleteListener {
                if (it.isSuccessful){
                    callback(true, "Password reset link sent to ${email}")
                }else{
                    callback(false, "${it.exception?.message}")
                }
            }
    }

    override fun getUserById(
        userId: String,
        callback: (Boolean, String, UserModel?) -> Unit
    ) {

        ref.child(userId).addListenerForSingleValueEvent(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {

                if(snapshot.exists()){

                    val user = snapshot.getValue(UserModel::class.java)

                    if (user != null){
                        callback(true, "User fetched", user)
                    }
                }

            }

            override fun onCancelled(error: DatabaseError) {
                callback(false, "${error.message}", null)
            }
        })
    }

    override fun getAllUsers(callback: (Boolean, String, List<UserModel>) -> Unit) {

        ref.addValueEventListener(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {

                if (snapshot.exists()){

                    var allUsers = mutableListOf<UserModel>()

                    for (data in snapshot.children){
                        val user = data.getValue(UserModel::class.java)
                        if (user != null){
                            allUsers.add(user)
                        }
                    }

                    callback(true, "Allusers fetched", allUsers)
                }
                else {
                    // No users node or empty — return empty list instead of doing nothing
                    callback(true, "No users found", emptyList())
                }

            }

            override fun onCancelled(error: DatabaseError) {

                // Log the error message to the console
                Log.e("UserRepo", "Firebase Read Cancelled! Code: ${error.code}. Message: ${error.message}", error.toException())
                callback(false, "${error.message}", emptyList())
            }
        })
    }

    override fun editUserProfile(
        userId: String,
        model: UserModel,
        callback: (Boolean, String) -> Unit
    ) {

        ref.child(userId).updateChildren(model.toMap())
            .addOnCompleteListener {
                if (it.isSuccessful){
                    callback(true, "User Account updated")
                }else{
                    callback(false, "${it.exception?.message}")
                }
            }

    }

    override fun deleteUser(
        userId: String,
        callback: (Boolean, String) -> Unit
    ) {
        ref.child(userId).removeValue()
            .addOnCompleteListener {
                if (it.isSuccessful){
                    callback(true, "User Account Deleted")
                }else{
                    callback(false, "${it.exception?.message}")
                }
            }
    }

    override fun saveUserLocation(
        userId: String,
        latitude: Double,
        longitude: Double,
        callback: (Boolean, String) -> Unit
    ) {
        val loactionUpdates = mapOf<String, Any?>(
            "latitude" to latitude,
            "longitude" to longitude
        )

        ref.child(userId).updateChildren(loactionUpdates)
            .addOnCompleteListener {
                if (it.isSuccessful) {
                    callback(true, "User location saved successfully.")
                } else {
                    callback(false, "Failed to save location: ${it.exception?.message}")
                }
            }
    }
}