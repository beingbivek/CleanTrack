package com.example.cleantrack.repository

import com.example.cleantrack.model.NotificationModel
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class NotificationRepoImpl : NotificationRepo {
    private val ref: DatabaseReference = FirebaseDatabase.getInstance().getReference("Notifications")
    private val userRepo = UserRepoImpl()

    override fun sendNotification(
        recipientId: String,
        recipientRole: String,
        title: String,
        message: String,
        type: String,
        metadata: Map<String, String>,
        callback: (Boolean, String) -> Unit
    ) {
        if (recipientId.isBlank()) {
            callback(false, "Missing recipient")
            return
        }
        val notificationId = ref.child(recipientId).push().key ?: run {
            callback(false, "Failed to create notification")
            return
        }
        val model = NotificationModel(
            notificationId = notificationId,
            recipientId = recipientId,
            recipientRole = recipientRole,
            title = title,
            message = message,
            type = type,
            metadata = metadata,
            createdAt = System.currentTimeMillis(),
            read = false
        )
        ref.child(recipientId).child(notificationId)
            .setValue(model)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    callback(true, "Notification sent")
                } else {
                    callback(false, task.exception?.message ?: "Failed to send notification")
                }
            }
    }

    override fun sendToUsers(
        userIds: List<String>,
        recipientRole: String,
        title: String,
        message: String,
        type: String,
        metadata: Map<String, String>
    ) {
        userIds.distinct().forEach { userId ->
            sendNotification(
                recipientId = userId,
                recipientRole = recipientRole,
                title = title,
                message = message,
                type = type,
                metadata = metadata
            )
        }
    }

    override fun sendToRole(
        role: String,
        title: String,
        message: String,
        type: String,
        metadata: Map<String, String>
    ) {
        userRepo.getAllUsers { success, _, users ->
            if (success) {
                val recipients = users.filter { it.role.equals(role, ignoreCase = true) }
                sendToUsers(
                    recipients.map { it.userId },
                    role,
                    title,
                    message,
                    type,
                    metadata
                )
            }
        }
    }

    override fun observeNotifications(
        recipientId: String,
        callback: (Boolean, String, List<NotificationModel>) -> Unit
    ) {
        ref.child(recipientId).addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val notifications = snapshot.children.mapNotNull {
                    it.getValue(NotificationModel::class.java)
                }.sortedByDescending { it.createdAt }
                callback(true, "Notifications loaded", notifications)
            }

            override fun onCancelled(error: DatabaseError) {
                callback(false, error.message, emptyList())
            }
        })
    }

    override fun markAsRead(recipientId: String, notificationId: String) {
        if (recipientId.isBlank() || notificationId.isBlank()) return
        ref.child(recipientId).child(notificationId).child("read").setValue(true)
    }
}
