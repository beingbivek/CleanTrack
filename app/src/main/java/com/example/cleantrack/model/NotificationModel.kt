package com.example.cleantrack.model

import kotlin.to

data class NotificationModel(
    val notificationId: String = "",
    val recipientId: String = "",
    val title: String = "",
    val message: String = "",
    val type: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val read: Boolean = false,
    val actionType: String = "",
    val routeId: String = "",
    val scheduleId: String = "",
    val productId: String = "",
    val ticketId: String = ""
) {
    fun toMap(): Map<String, Any?> {
        return mapOf(
            "notificationId" to notificationId,
            "recipientId" to recipientId,
            "title" to title,
            "message" to message,
            "type" to type,
            "timestamp" to timestamp,
            "read" to read,
            "actionType" to actionType,
            "routeId" to routeId,
            "scheduleId" to scheduleId,
            "productId" to productId,
            "ticketId" to ticketId
        )
    }
}

data class NotificationPayload(
    val title: String,
    val message: String,
    val type: String,
    val actionType: String = "",
    val routeId: String = "",
    val scheduleId: String = "",
    val productId: String = "",
    val ticketId: String = ""
)
