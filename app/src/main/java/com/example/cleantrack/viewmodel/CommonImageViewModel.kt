package com.example.cleantrack.viewmodel

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import com.example.cleantrack.repository.CommonImageRepo


class CommonImageViewModel(val repo : CommonImageRepo) : ViewModel() {

    fun uploadImage(context : Context, imageUri: Uri, callback : (String?)-> Unit){
        repo.uploadImage(context, imageUri, callback)

    }
}