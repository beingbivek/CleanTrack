package com.example.cleantrack.repository

import android.content.Context
import android.net.Uri


interface CommonImageRepo {


    fun uploadImage(context : Context, imageUri: Uri, callback : (String?)-> Unit)

    fun getFileNameFromUri(context: Context, uri: Uri): String?
}