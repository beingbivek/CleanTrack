package com.example.cleantrack.repository

import com.example.cleantrack.model.NotificationModel

interface NotificationRepo {
    fun sendNotification(
        recipientId: String,
        recipientRole: String,
        title: String,
        message: String,
        type: String,
        metadata: Map<String, String> = emptyMap(),
        callback: (Boolean, String) -> Unit = { _, _ -> }
    )

    fun sendToUsers(
        userIds: List<String>,
        recipientRole: String,
        title: String,
        message: String,
        type: String,
        metadata: Map<String, String> = emptyMap()
    )

    fun sendToRole(
        role: String,
        title: String,
        message: String,
        type: String,
        metadata: Map<String, String> = emptyMap()
    )

    fun observeNotifications(
        recipientId: String,
        callback: (Boolean, String, List<NotificationModel>) -> Unit
    )

    fun markAsRead(recipientId: String, notificationId: String)
}
