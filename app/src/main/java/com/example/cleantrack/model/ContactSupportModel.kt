package com.example.cleantrack.model

import android.os.Parcelable
import com.google.firebase.database.Exclude
import kotlinx.parcelize.Parcelize

@Parcelize
data class ContactSupportModel(
    val issueId: String = "",
    val userId: String = "",
    val userType: String = "GUEST", // REGISTERED | GUEST
    val fullname: String = "",
    val email: String = "",
    val category: String = "",
    val message: String = "",
    val attachmentUrl: String = "",
    val timestamp: Long = System.currentTimeMillis()
) : Parcelable {

    @Exclude
    fun toMap(): Map<String, Any?> {
        return mapOf(
            "issueId" to issueId,
            "userId" to userId,
            "userType" to userType,
            "fullname" to fullname,
            "email" to email,
            "category" to category,
            "message" to message,
            "attachmentUrl" to attachmentUrl,
            "timestamp" to timestamp
        )
    }
}
