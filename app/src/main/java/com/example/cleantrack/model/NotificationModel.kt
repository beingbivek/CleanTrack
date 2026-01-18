package com.example.cleantrack.model

data class NotificationModel(
    val notificationId: String = "",
    val recipientId: String = "",
    val recipientRole: String = "",
    val title: String = "",
    val message: String = "",
    val type: String = "",
    val metadata: Map<String, String> = emptyMap(),
    val createdAt: Long = System.currentTimeMillis(),
    val read: Boolean = false
)
