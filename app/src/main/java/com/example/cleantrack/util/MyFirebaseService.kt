package com.example.cleantrack.util

import android.util.Log
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class MyFirebaseService : FirebaseMessagingService() {

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d("FCM_TOKEN", "New Token: $token")
        // OPTIONAL: send token to your backend
    }

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
        Log.d("FCM_MESSAGE", "Message received: ${message.data}")

        // If the notification comes from "notification" payload
        message.notification?.let {
            Log.d("FCM_NOTIFICATION", "Title: ${it.title}, Body: ${it.body}")
        }
    }
}
