package com.example.cleantrack.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.cleantrack.model.NotificationModel
import com.example.cleantrack.model.NotificationPayload
import com.example.cleantrack.model.UserModel
import com.example.cleantrack.repository.NotificationRepo
import com.example.cleantrack.repository.UserRepo
import kotlin.collections.distinctBy
import kotlin.collections.filter
import kotlin.collections.forEach
import kotlin.text.isBlank
import kotlin.text.uppercase

class NotificationViewModel(
    private val notificationRepo: NotificationRepo,
    private val userRepo: UserRepo
) : ViewModel() {

    private val _notifications = MutableLiveData<List<NotificationModel>>()
    val notifications: MutableLiveData<List<NotificationModel>>
        get() = _notifications

    fun observeNotifications(recipientId: String) {
        notificationRepo.getNotificationsForRecipient(recipientId) { success, _, data ->
            if (success) _notifications.postValue(data)
        }
    }

    fun markAsRead(recipientId: String, notificationId: String, callback: (Boolean, String) -> Unit) {
        notificationRepo.markAsRead(recipientId, notificationId, callback)
    }

    fun notifyAllAdmins(payload: NotificationPayload) {
        userRepo.getAllUsers { success, _, users ->
            if (!success) return@getAllUsers
            val admins = users.filter { it.role.uppercase() == "ADMIN" }
            sendNotifications(admins, payload)
        }
    }

    fun notifyAllRecipients(payload: NotificationPayload) {
        userRepo.getAllUsers { success, _, users ->
            if (!success) return@getAllUsers
            sendNotifications(users, payload)
        }
    }

    fun notifyAllUsersAndDrivers(payload: NotificationPayload) {
        userRepo.getAllUsers { success, _, users ->
            if (!success) return@getAllUsers
            val recipients = users.filter {
                val role = it.role.uppercase()
                role == "USER" || role == "DRIVER"
            }
            sendNotifications(recipients, payload)
        }
    }

    fun notifyUsersByRoute(routeId: String, payload: NotificationPayload) {
        userRepo.getUsersByRoute(routeId) { success, _, users ->
            if (!success || users == null) return@getUsersByRoute
            sendNotifications(users, payload)
        }
    }

    fun notifyDriver(driverId: String, payload: NotificationPayload) {
        if (driverId.isBlank()) return
        userRepo.getUserById(driverId) { success, _, user ->
            if (success && user != null) sendNotifications(listOf(user), payload)
        }
    }

    fun notifyUser(userId: String, payload: NotificationPayload) {
        if (userId.isBlank()) return
        userRepo.getUserById(userId) { success, _, user ->
            if (success && user != null) sendNotifications(listOf(user), payload)
        }
    }

    fun notifyUsersByIds(userIds: Collection<String>, payload: NotificationPayload) {
        if (userIds.isEmpty()) return
        userRepo.getAllUsers { success, _, users ->
            if (!success) return@getAllUsers
            val recipients = users.filter { userIds.contains(it.userId) }
            sendNotifications(recipients, payload)
        }
    }

    private fun sendNotifications(recipients: List<UserModel>, payload: NotificationPayload) {
        recipients.distinctBy { it.userId }.forEach { user ->
            val model = NotificationModel(
                recipientId = user.userId,
                title = payload.title,
                message = payload.message,
                type = payload.type,
                actionType = payload.actionType,
                routeId = payload.routeId,
                scheduleId = payload.scheduleId,
                productId = payload.productId,
                ticketId = payload.ticketId
            )
            notificationRepo.addNotification(model) { _, _ -> }
        }
    }
}
