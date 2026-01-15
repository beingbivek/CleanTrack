package com.example.cleantrack.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cleantrack.model.SubscriptionModel
import com.example.cleantrack.model.UserModel
import com.example.cleantrack.repository.PaymentRepo
import com.example.cleantrack.repository.UserRepo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

class PaymentViewModel(
    private val paymentRepo: PaymentRepo,
    private val userRepo: UserRepo // Need this to get current User ID/Email
) : ViewModel() {

    private val _paymentStatus = MutableLiveData<PaymentState>()
    val paymentStatus: LiveData<PaymentState> = _paymentStatus

    private val _currentSubscription = MutableLiveData<SubscriptionModel>()
    val currentSubscription: LiveData<SubscriptionModel> = _currentSubscription

    fun loadSubscriptionStatus() {
        val userId = userRepo.getCurrentUserId() ?: return
        viewModelScope.launch {
            paymentRepo.checkSubscription(userId) { sub ->
                _currentSubscription.postValue(sub)
            }
        }
    }

    fun processMonthlySubscription(amount: String) {
        val userId = userRepo.getCurrentUserId()
        if (userId == null) {
            _paymentStatus.value = PaymentState.Error("User not logged in")
            return
        }

        _paymentStatus.value = PaymentState.Loading

        viewModelScope.launch(Dispatchers.IO) {
            try {
                // 1. Fetch user profile using your existing callback-based method
                val user = suspendCancellableCoroutine<UserModel?> { continuation ->
                    userRepo.getUserById(userId) { success, message, userModel ->
                        if (success && userModel != null) {
                            continuation.resume(userModel)
                        } else {
                            continuation.resume(null)
                        }
                    }
                }

                if (user == null) {
                    _paymentStatus.postValue(PaymentState.Error("Could not retrieve user profile info"))
                    return@launch
                }

                // 2. Extract real data (assuming UserModel has 'email' and 'phone')
                // Using elvis operators as fallbacks to prevent API crashes
                val email = user.email.ifEmpty { "no-email@cleantrack.com" }
                val phone = user.number.ifEmpty { "0000000000" }

                // 3. Initiate Payment with REAL user info
                val payResult = paymentRepo.initiatePayment(amount, email, phone)

                if (payResult.isSuccess) {
                    val txnId = payResult.getOrNull() ?: "UNKNOWN"

                    // 4. Update Database Subscription
                    val dbResult = paymentRepo.activateSubscription(userId, txnId)

                    if (dbResult.isSuccess) {
                        _paymentStatus.postValue(PaymentState.Success("Subscription Active!"))
                        loadSubscriptionStatus()
                    } else {
                        _paymentStatus.postValue(PaymentState.Error("Paid successfully, but failed to update status. TXN: $txnId"))
                    }
                } else {
                    val errorMsg = payResult.exceptionOrNull()?.message ?: "Payment Failed"
                    _paymentStatus.postValue(PaymentState.Error(errorMsg))
                }

            } catch (e: Exception) {
                _paymentStatus.postValue(PaymentState.Error("System Error: ${e.localizedMessage}"))
            }
        }
    }
}

// Sealed class for UI State
sealed class PaymentState {
    object Idle : PaymentState()
    object Loading : PaymentState()
    data class Success(val message: String) : PaymentState()
    data class Error(val message: String) : PaymentState()
}