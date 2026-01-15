package com.example.cleantrack.viewmodel

import androidx.lifecycle.ViewModel
import com.example.cleantrack.repository.TermsAndConditionRepo
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class TermsAndConditionViewModel(private val repository: TermsAndConditionRepo) : ViewModel() {
    private val _termsAndCondition = MutableStateFlow("")
    val termsAndCondition: StateFlow<String> = _termsAndCondition

    private val _message = MutableStateFlow("")
    val message: StateFlow<String> = _message

    fun loadTermsAndCondition() {
        repository.getTermsAndCondition { success, _, content ->
            if (success) _termsAndCondition.value = content ?: ""
        }
    }

    fun postTermsAndCondition(content: String, callback: (Boolean, String) -> Unit) {
        repository.updateTermsAndCondition(content, callback)
    }
}