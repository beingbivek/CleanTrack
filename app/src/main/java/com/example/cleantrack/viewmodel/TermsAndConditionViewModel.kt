package com.example.cleantrack.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.cleantrack.repository.TermsAndConditionRepo
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class TermsAndConditionViewModel(private val repository: TermsAndConditionRepo) : ViewModel() {
    private val _termsAndCondition = MutableLiveData("")
    val termsAndCondition: LiveData<String> get() = _termsAndCondition


    private val _message = MutableLiveData("")
    val message: LiveData<String> get() = _message

    fun loadTermsAndCondition() {
        repository.getTermsAndCondition { success, _, content ->
            if (success) _termsAndCondition.value = content ?: ""
        }
    }

    fun postTermsAndCondition(content: String, callback: (Boolean, String) -> Unit) {
        repository.updateTermsAndCondition(content, callback)
    }
}