package com.example.cleantrack.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cleantrack.model.SubscriptionModel
import com.example.cleantrack.repository.PaymentRepo
import com.example.cleantrack.repository.PointsRepo
import com.example.cleantrack.repository.UserRepo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class PaymentViewModel(
    private val paymentRepo: PaymentRepo,
    private val userRepo: UserRepo,
    private val pointsRepo: PointsRepo
) : ViewModel() {

    private val _paymentStatus = MutableLiveData<PaymentState>()
    val paymentStatus: LiveData<PaymentState> = _paymentStatus

    private val _currentSubscription = MutableLiveData<SubscriptionModel>()
    val currentSubscription: LiveData<SubscriptionModel> = _currentSubscription

    private val _subscriptionAmount = MutableLiveData<String>()
    val subscriptionAmount: LiveData<String> = _subscriptionAmount

    fun loadSubscriptionAmount() {
        viewModelScope.launch {
            val result = paymentRepo.getSubscriptionAmount()
            _subscriptionAmount.postValue(result.getOrDefault("500"))
        }
    }

    fun loadSubscriptionStatus() {
        val userId = userRepo.getCurrentUserId() ?: return
        viewModelScope.launch {
            paymentRepo.checkSubscription(userId) { sub ->
                _currentSubscription.postValue(sub)
            }
        }
    }

    fun processMonthlySubscription(amount: String) {
        viewModelScope.launch(Dispatchers.IO) {
            _paymentStatus.postValue(PaymentState.Loading)

            val user = userRepo.getCurrentUser()

            if (user == null) {
                _paymentStatus.postValue(PaymentState.Error("User profile not found. Please log in again."))
                return@launch
            }

            val initiationResult = paymentRepo.initiatePayment(amount, user.email, user.number)

            if (initiationResult.isSuccess) {
                val initiation = initiationResult.getOrNull()!!
                _paymentStatus.postValue(
                    PaymentState.LaunchStripe(
                        paymentIntentId = initiation.paymentIntentId,
                        clientSecret = initiation.clientSecret
                    )
                )
            } else {
                _paymentStatus.postValue(PaymentState.Error("Failed to initiate: ${initiationResult.exceptionOrNull()?.message}"))
            }
        }
    }

    fun processPointsPayment(pointsNeeded: Int) {
        val userId = userRepo.getCurrentUserId() ?: return

        viewModelScope.launch {
            _paymentStatus.postValue(PaymentState.Loading)

            // 1. Deduct points first
            pointsRepo.deductPoints(userId, pointsNeeded, "Redeemed for Monthly Fee") { success, message ->
                if (success) {
                    // 2. If points deduction works, activate subscription with a special ID
                    val transactionId = "POINTS_REDEM_${System.currentTimeMillis()}"

                    viewModelScope.launch {
                        val result = paymentRepo.activateSubscription(userId, transactionId)
                        if (result.isSuccess) {
                            _paymentStatus.postValue(PaymentState.Success("Fee paid with points!"))
                        } else {
                            _paymentStatus.postValue(PaymentState.Error("Points taken, but activation failed. Contact support."))
                        }
                    }
                } else {
                    _paymentStatus.postValue(PaymentState.Error(message))
                }
            }
        }
    }

    fun completePayment(userId: String, transactionId: String) {
        viewModelScope.launch {
            _paymentStatus.postValue(PaymentState.Loading)
            val result = paymentRepo.activateSubscription(userId, transactionId)
            if (result.isSuccess) {
                _paymentStatus.postValue(PaymentState.Success("Subscription activated successfully!"))
            } else {
                _paymentStatus.postValue(PaymentState.Error("Activation failed: ${result.exceptionOrNull()?.message}"))
            }
        }
    }

    fun resetPaymentStatus() {
        _paymentStatus.value = PaymentState.Idle
    }
}

// Sealed class for UI State
sealed class PaymentState {
    object Idle : PaymentState()
    object Loading : PaymentState()
    data class LaunchStripe(
        val paymentIntentId: String,
        val clientSecret: String
    ) : PaymentState()
    data class Success(val message: String) : PaymentState()
    data class Error(val message: String) : PaymentState()
}
