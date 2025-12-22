package com.example.cleantrack.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class ContactSupportModel(
    val issueId: String = "",
    val userId: String = "",
    val userType: String = "GUEST",
    val fullname: String = "",
    val email: String = "",
    val category: String = "",
    val message: String = "",
    val attachmentUrl: String = "",
    val timestamp: Long = System.currentTimeMillis()
) : Parcelable {
}
