package com.example.cleantrack.model

import android.os.Parcelable
import com.google.firebase.database.Exclude
import kotlinx.parcelize.Parcelize

@Parcelize
data class ContactSupportModel(
    val ticketId: String = "",
    val userId: String = "",
    val fullname: String = "",
    val email: String = "",
    val category: String = "",
    val message: String = "",
    val adminReply: String = "",
    val attachmentUrl: String = "",
    val userType: String = "Guest",
    val status: String = "OPEN",
    val timestamp: Long = System.currentTimeMillis()
) : Parcelable {

    fun createReply(text: String): ContactSupportModel {
        return this.copy(adminReply = text, status = "REPLIED")
    }
}