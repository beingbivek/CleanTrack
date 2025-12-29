package com.example.cleantrack.viewmodel

import ContactSupportRepoImpl
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.cleantrack.model.ContactSupportModel
import com.example.cleantrack.repository.ContactSupportRepo
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ContactSupportViewModel(val repo: ContactSupportRepo) : ViewModel() {
    val loading = MutableLiveData<Boolean>()
    val currentUserData = MutableLiveData<ContactSupportModel?>()
    val allIssues = MutableLiveData<List<ContactSupportModel>>()
    val adminMessage = MutableLiveData<String>()

    fun fetchInitialData() {
        repo.getCurrentUserDetails { user ->
            currentUserData.postValue(user?.let {
                ContactSupportModel(fullname = it.fullname, email = it.email, userId = it.userId, userType = "Registered")
            } ?: ContactSupportModel(userType = "Guest"))
        }
    }

    fun submitTicket(fullname: String, email: String, category: String, message: String, userId: String, userType: String,
                     attachmentUrl: String, callback: (Boolean, String) -> Unit) {
        if (category == "Select Issue" || message.isBlank()) return callback(false, "Complete all fields")
        loading.value = true
        val ticket = ContactSupportModel(fullname = fullname, email = email, category = category, message = message, userId = userId,
            userType = userType,
            attachmentUrl = attachmentUrl)
        repo.submitIssue(ticket) { s, m -> loading.value = false; callback(s, m) }
    }

    fun fetchAllTickets() = repo.getAllIssues { s, m, d -> if(s) allIssues.postValue(d) }

    fun changeStatus(ticket: ContactSupportModel, newStatus: String) {
        repo.updateTicketStatus(ticket.ticketId, ticket.category, newStatus) { s, m -> adminMessage.postValue(m) }
    }

    fun userReplyToTicket(issue: ContactSupportModel, userReply: String, callback: (Boolean) -> Unit) {
        val timestamp = SimpleDateFormat("dd MMM, HH:mm", Locale.getDefault()).format(Date())

        val updatedThread = "${issue.message}\n\n[${issue.fullname} @ $timestamp]: $userReply"

        val updatedIssue = issue.copy(message = updatedThread, status = "OPEN")

        (repo as ContactSupportRepoImpl).updateIssueThread(updatedIssue) { success ->
            if (success) fetchAllTickets()
            callback(success)
        }
    }

    fun adminReplyToTicket(issue: ContactSupportModel, adminReply: String) {
        val timestamp = SimpleDateFormat("dd MMM, HH:mm", Locale.getDefault()).format(Date())
        val updatedThread = "${issue.message}\n\n[Admin @ $timestamp]: $adminReply"
        val updatedIssue = issue.copy(
            message = updatedThread,
            status = "REPLIED",
            timestamp = System.currentTimeMillis()
        )

        repo.sendAdminReply(updatedIssue) { success ->
            if (success) fetchAllTickets()
        }
    }
}