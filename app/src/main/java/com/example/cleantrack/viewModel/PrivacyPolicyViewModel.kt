package com.example.cleantrack.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.cleantrack.model.PrivacyPolicyModel
import com.example.cleantrack.repository.PrivacyPolicyRepo

class PrivacyPolicyViewModel(val repo: PrivacyPolicyRepo) : ViewModel() {

    private val _policy = MutableLiveData<PrivacyPolicyModel?>()
    val policy: MutableLiveData<PrivacyPolicyModel?>
        get() = _policy

    private val _loading = MutableLiveData<Boolean>()
    val loading: MutableLiveData<Boolean>
        get() = _loading

    private val _message = MutableLiveData<String?>()
    val message: MutableLiveData<String?>
        get() = _message

    fun loadPrivacyPolicy() {
        _loading.postValue(true)
        repo.getPrivacyPolicy { success, msg, data ->
            _loading.postValue(false)
            _message.postValue(msg)
            if (success) _policy.postValue(data)
        }
    }

    fun savePrivacyPolicy(description: String, callback: (Boolean, String) -> Unit) {
        _loading.postValue(true)

        val model = PrivacyPolicyModel(
            privacypolicyId = "policy_1",
            description = description.trim(),
            date = System.currentTimeMillis()
        )

        repo.savePrivacyPolicy(model) { success, msg ->
            _loading.postValue(false)
            _message.postValue(msg)
            if (success) _policy.postValue(model)
            callback(success, msg)
        }
    }
}
