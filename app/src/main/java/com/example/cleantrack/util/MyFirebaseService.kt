package com.example.cleantrack.util

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class MyFirebaseService : FirebaseMessagingService() {

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d("FCM_TOKEN", "New Token: $token")

        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (!userId.isNullOrBlank()) {
            FirebaseDatabase.getInstance()
                .getReference("Users")
                .child(userId)
                .child("fcmToken")
                .setValue(token)
        }
    }

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
        Log.d("FCM_MESSAGE", "Message received: ${message.data}")

        // If the notification comes from "notification" payload
        message.notification?.let {
            Log.d("FCM_NOTIFICATION", "Title: ${it.title}, Body: ${it.body}")
            showNotification(it.title ?: "CleanTrack", it.body ?: "")
        }

        // If the notification comes from "data" payload
        if (message.data.isNotEmpty()) {
            val title = message.data["title"] ?: "CleanTrack"
            val body = message.data["body"] ?: ""
            if (body.isNotBlank()) {
                showNotification(title, body)
            }
        }
    }

    private fun showNotification(title: String, body: String) {
        NotificationHelper.showSystemNotification(this, title, body)
    }
}
