package com.example.cleantrack.viewmodel

import android.R
import androidx.lifecycle.ViewModel
import com.example.cleantrack.model.UserModel
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.GoogleAuthCredential
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.auth
import com.google.firebase.firestore.firestore

class AuthViewModel : ViewModel() {

    private val auth = Firebase.auth

    private val firestore = Firebase.firestore

    private fun isValidPhone(number: String) = number.length == 10 && number.all { it.isDigit() }

    fun signup(email: String,fullname : String,
               number: String, password : String,
               confirmpassword: String,
               role: String = "USER",
               onResult:(Boolean, String?)-> Unit){

        if (!isValidPhone(number)) { onResult(false, "Please enter a valid phone number"); return }

        auth.createUserWithEmailAndPassword(email,password)
            .addOnCompleteListener {
                if (it.isSuccessful){

                    var userId = it.result?.user?.uid

                    val userModel = UserModel(email, fullname , number,role, userId!!)

                    firestore.collection("users").document(userId)
                        .set(userModel)
                        .addOnCompleteListener { dbTask->
                            if (dbTask.isSuccessful){
                                onResult(true,null)

                            }else   {
                                onResult(false,"Something went wrong!")
                            }
                        }

                }else{
                    onResult(false,it.exception?.localizedMessage)
                }
            }

    }

    fun login(email: String, password: String, onResult: (Boolean, String?, String?) -> Unit){

        auth.signInWithEmailAndPassword(email   ,password)
            .addOnCompleteListener {
                if (it.isSuccessful){
                    var userId = it.result?.user?.uid

                    if (userId != null){

                        firestore.collection("users").document(userId)
                            .get()
                            .addOnSuccessListener {
                                documentSnapshot ->
                                val role = documentSnapshot.getString("role")
                                if (role != null){

                                    onResult(true,null,role)
                                }else{
                                    onResult(false, "Login successful, but user role not defined.", null)

                                }

                            }
                            .addOnFailureListener {
                                onResult(false,"Login successfull, but faild to fetch userData",null)
                            }



                    }else{
                        onResult(false, "Login Successfull, but userID is missing.", null)
                    }
                }else   {
                    onResult(false,it.exception?.localizedMessage, null)
                }
            }

    }

    fun signInWithGoogle(idToken: String, onResult: (Boolean, String?, String?) -> Unit){
        val credential = GoogleAuthProvider.getCredential(idToken,null)

        auth.signInWithCredential(credential)
            .addOnCompleteListener { authTask ->
                if (authTask.isSuccessful){
                    val user = authTask.result?.user
                    val userId = user?.uid

                    if (userId != null && user.email != null){

                        // checks if user document already exists in firestore
                        firestore.collection("users").document(userId)
                            .get()
                            .addOnSuccessListener { documentSnapshot ->
                                if(documentSnapshot.exists()){

                                    // user exists, retrieve their role
                                    val role = documentSnapshot.getString("role") ?: "USER"
                                    onResult(true, null, role)
                                } else  {

                                    // New user : create its firestore document
                                    val defaultRole = "USER"
                                    val userModel = UserModel(
                                        email = user.email!!,
                                        fullname = user.displayName ?: "New User",
                                        number = "",
                                        role = defaultRole,
                                        userId = userId
                                    )

                                    firestore.collection("users")
                                        .document(userId)
                                        .set(userModel)
                                        .addOnSuccessListener {
                                            onResult(true, null, defaultRole)
                                        }
                                        .addOnFailureListener { dbError ->
                                            onResult(false, "Google sign-in succeeded, but failed to create user document : ${dbError.localizedMessage}", null)
                                        }
                                }
                            }
                            .addOnFailureListener { fetchError ->
                                onResult(false, "Google sign-in succeeded, but failed to check user document: ${fetchError.localizedMessage}", null)
                            }

                    }else{
                        onResult(false, "Google sign-in succeeded, but missing required user details (ID/Email).", null)
                    }

                }else{
                    onResult(false, authTask.exception?.localizedMessage, null)
                }
            }
    }

    fun forgotPassword(email : String, onResult: (Boolean,  String?) -> Unit){
        if (email.isBlank()){
            onResult(false,"Please enter your email address.")
            return
        }

        auth.sendPasswordResetEmail(email   )
            .addOnCompleteListener { task ->
                if (task.isSuccessful){
                    onResult(true, null)
                }else   {
                    // --- MODIFIED ERROR HANDLING ---
                    val errorMessage = when (task.exception) {
                        is FirebaseAuthInvalidUserException ->
                            "The email address is not registered."
                        is FirebaseAuthInvalidCredentialsException ->
                            "The email address format is invalid."
                        else ->
                            // Log the full exception for debugging in Logcat
                            // Log.e("AuthViewModel", "Reset Failed", task.exception)
                            task.exception?.localizedMessage ?: "Failed to send reset email (Generic Error)."
                    }
                    onResult(false, errorMessage)
                    // --- END MODIFIED ERROR HANDLING ---
                }
            }
    }
}

