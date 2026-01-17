package com.example.cleantrack.repository

import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.provider.OpenableColumns
import com.cloudinary.Cloudinary
import com.cloudinary.utils.ObjectUtils
import com.google.android.gms.tasks.Tasks
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import java.io.InputStream
import java.util.concurrent.Executors

class CommonImageRepoImpl: CommonImageRepo {
    override fun uploadImage(
        context: Context,
        imageUri: Uri,
        callback: (String?) -> Unit
    ) {
        val executor = Executors.newSingleThreadExecutor()
        executor.execute {
            try {
                val remoteConfig = FirebaseRemoteConfig.getInstance()
                Tasks.await(remoteConfig.fetchAndActivate())
                val cloudName = remoteConfig.getString("cloudinary_cloud_name")
                    .takeIf { it.isNotBlank() }
                val apiKey = remoteConfig.getString("cloudinary_api_key")
                    .takeIf { it.isNotBlank() }
                val apiSecret = remoteConfig.getString("cloudinary_secret_key")
                    .takeIf { it.isNotBlank() }

                if (cloudName == null || apiKey == null || apiSecret == null) {
                    Handler(Looper.getMainLooper()).post {
                        callback(null)
                    }
                    return@execute
                }

                val cloudinary = Cloudinary(
                    mapOf(
                        "cloud_name" to cloudName,
                        "api_key" to apiKey,
                        "api_secret" to apiSecret
                    )
                )

                val inputStream: InputStream? = context.contentResolver.openInputStream(imageUri)
                var fileName = getFileNameFromUri(context, imageUri)

                fileName = fileName?.substringBeforeLast(".") ?: "uploaded_image"

                val response = cloudinary.uploader().upload(
                    inputStream, ObjectUtils.asMap(
                        "public_id", fileName,
                        "resource_type", "image"
                    )
                )

                var imageUrl = response["url"] as String?

                imageUrl = imageUrl?.replace("http://", "https://")

                Handler(Looper.getMainLooper()).post {
                    callback(imageUrl)
                }

            } catch (e: Exception) {
                e.printStackTrace()
                Handler(Looper.getMainLooper()).post {
                    callback(null)
                }
            }
        }
    }

    override fun getFileNameFromUri(
        context: Context,
        uri: Uri
    ): String? {
        var fileName: String? = null
        val cursor: Cursor? = context.contentResolver.query(uri, null, null, null, null)
        cursor?.use {
            if (it.moveToFirst()) {
                val nameIndex = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (nameIndex != -1) {
                    fileName = it.getString(nameIndex)
                }
            }
        }
        return fileName
    }
}
