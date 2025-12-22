package com.example.cleantrack.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.cleantrack.model.ContactSupportModel
import com.example.cleantrack.repository.ContactSupportRepo

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

    fun submitTicket(fullname: String, email: String, category: String, message: String, userId: String, userType: String, callback: (Boolean, String) -> Unit) {
        if (category == "Select Issue" || message.isBlank()) return callback(false, "Complete all fields")
        loading.value = true
        val ticket = ContactSupportModel(fullname = fullname, email = email, category = category, message = message, userId = userId, userType = userType)
        repo.submitIssue(ticket) { s, m -> loading.value = false; callback(s, m) }
    }

    fun fetchAllTickets() = repo.getAllIssues { s, m, d -> if(s) allIssues.postValue(d) }

    fun changeStatus(ticket: ContactSupportModel, newStatus: String) {
        repo.updateTicketStatus(ticket.ticketId, ticket.category, newStatus) { s, m -> adminMessage.postValue(m) }
    }
}