package com.example.cleantrack.repository

import com.example.cleantrack.model.ContactSupportModel
import com.example.cleantrack.model.UserModel


interface ContactSupportRepo {
    fun submitIssue(model: ContactSupportModel, callback: (Boolean, String) -> Unit)
    fun getCurrentUserDetails(callback: (UserModel?) -> Unit)
    fun getAllIssues(callback: (Boolean, String, List<ContactSupportModel>) -> Unit)
    fun updateTicketStatus(ticketId: String, category: String, newStatus: String, callback: (Boolean, String) -> Unit)
    fun sendAdminReply(model: ContactSupportModel, callback: (Boolean) -> Unit)}

