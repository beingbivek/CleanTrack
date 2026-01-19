package com.example.cleantrack.repository

import com.example.cleantrack.model.NotificationModel
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class NotificationRepoImpl : NotificationRepo {
    private val ref = FirebaseDatabase.getInstance().getReference("Notifications")

    override fun addNotification(model: NotificationModel, callback: (Boolean, String) -> Unit) {
        val id = ref.push().key ?: return callback(false, "ID error")
        val finalModel = model.copy(notificationId = id)
        ref.child(id).setValue(finalModel).addOnCompleteListener {
            if (it.isSuccessful) callback(true, "Notification saved")
            else callback(false, it.exception?.message ?: "Failed")
        }
    }

    override fun getNotificationsForRecipient(
        recipientId: String,
        callback: (Boolean, String, List<NotificationModel>) -> Unit
    ) {
        ref.orderByChild("recipientId")
            .equalTo(recipientId)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val list = snapshot.children.mapNotNull {
                        it.getValue(NotificationModel::class.java)
                    }.sortedByDescending { it.timestamp }
                    callback(true, "Success", list)
                }

                override fun onCancelled(error: DatabaseError) {
                    callback(false, error.message, emptyList())
                }
            })
    }

    override fun markAsRead(
        recipientId: String,
        notificationId: String,
        callback: (Boolean, String) -> Unit
    ) {
        ref.child(notificationId).child("read").setValue(true)
            .addOnCompleteListener {
                if (it.isSuccessful) callback(true, "Read")
                else callback(false, it.exception?.message ?: "Failed")
            }
    }
}
