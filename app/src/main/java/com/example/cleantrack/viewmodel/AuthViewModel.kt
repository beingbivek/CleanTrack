package com.example.cleantrack.viewmodel

import android.R
import androidx.lifecycle.ViewModel
import com.example.cleantrack.model.UserModel
import com.google.firebase.Firebase
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
}