package com.example.cleantrack.viewmodel

import android.content.Context
import android.net.Uri
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.cleantrack.repository.CommonImageRepo

class CommonImageViewModel(val repo : CommonImageRepo) : ViewModel() {

    // Added loading state to be observed by the UI
    private val _loading = MutableLiveData<Boolean>(false)
    val loading: MutableLiveData<Boolean> get() = _loading

    fun uploadImage(context : Context, imageUri: Uri, callback : (String?)-> Unit){
        // 1. Start loading immediately on the main thread
        _loading.value = true

        repo.uploadImage(context, imageUri) { imageUrl ->
            // 2. Stop loading once the repository returns the result
            _loading.postValue(false)

            // 3. Send the result back to the Activity
            callback(imageUrl)
        }
    }
}