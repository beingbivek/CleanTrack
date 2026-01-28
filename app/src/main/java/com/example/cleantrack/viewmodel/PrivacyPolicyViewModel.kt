package com.example.cleantrack.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.cleantrack.repository.PrivacyPolicyRepo
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class PrivacyPolicyViewModel(private val repository: PrivacyPolicyRepo) : ViewModel() {
    private val _privacypolicy = MutableLiveData<String?>(null)
    val privacypolicy: LiveData<String?> get() = _privacypolicy

    private val _message = MutableLiveData("")
    val message: LiveData<String> get() = _message

    fun loadPrivacyPolicy() {
        repository.getPrivacyPolicy { success, msg, content ->
            if (success) _privacypolicy.value = content ?: ""
        }
    }

    fun postPrivacyPolicy(content: String, callback: (Boolean,String) -> Unit) {
        repository.updatePrivacyPolicy(content, callback)
    }
}
