package com.example.cleantrack.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cleantrack.model.SubscriptionModel
import com.example.cleantrack.repository.PaymentRepo
import com.example.cleantrack.repository.UserRepo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

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
            // 1. Fetch User Details for Payment (Optional, but good practice)
            // For now, using dummy or basic data
            val email = "user@cleantrack.com"
            val phone = "9800000000"

            // 2. Call Payment API
            val payResult = paymentRepo.initiatePayment(amount, email, phone)

            if (payResult.isSuccess) {
                val txnId = payResult.getOrNull() ?: "UNKNOWN"

                // 3. If Payment API succeeds, Update Firebase
                val dbResult = paymentRepo.activateSubscription(userId, txnId)

                if (dbResult.isSuccess) {
                    _paymentStatus.postValue(PaymentState.Success("Subscription Active! Valid for 30 Days."))
                    loadSubscriptionStatus() // Refresh UI
                } else {
                    _paymentStatus.postValue(PaymentState.Error("Payment charged but DB update failed. Contact Support: $txnId"))
                }
            } else {
                _paymentStatus.postValue(PaymentState.Error(payResult.exceptionOrNull()?.message ?: "Unknown Error"))
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