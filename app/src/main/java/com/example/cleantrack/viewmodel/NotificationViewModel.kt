package com.example.cleantrack.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.cleantrack.model.NotificationModel
import com.example.cleantrack.repository.NotificationRepo
import kotlin.text.isBlank

class NotificationViewModel(private val repo: NotificationRepo) : ViewModel() {
    val notifications = MutableLiveData<List<NotificationModel>>()
    val loading = MutableLiveData<Boolean>()

    fun loadNotifications(recipientId: String) {
        if (recipientId.isBlank()) return
        loading.postValue(true)
        repo.observeNotifications(recipientId) { success, _, data ->
            if (success) {
                notifications.postValue(data)
            }
            loading.postValue(false)
        }
    }

    fun markAsRead(recipientId: String, notificationId: String) {
        repo.markAsRead(recipientId, notificationId)
    }
}
