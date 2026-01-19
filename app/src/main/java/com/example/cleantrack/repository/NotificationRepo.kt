package com.example.cleantrack.repository

import com.example.cleantrack.model.NotificationModel

interface NotificationRepo {
    fun addNotification(model: NotificationModel, callback: (Boolean, String) -> Unit)
    fun getNotificationsForRecipient(recipientId: String, callback: (Boolean, String, List<NotificationModel>) -> Unit)
    fun markAsRead(recipientId: String, notificationId: String, callback: (Boolean, String) -> Unit)
}
