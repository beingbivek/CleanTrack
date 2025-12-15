package com.example.cleantrack.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cleantrack.model.PrivacyPolicyModel
import com.example.cleantrack.repository.PrivacyPolicyRepoImpl
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class PrivacyPolicyViewModel : ViewModel() {

    private val repo = PrivacyPolicyRepoImpl()

    private val _privacyPolicy = MutableStateFlow<PrivacyPolicyModel?>(null)
    val privacyPolicy: StateFlow<PrivacyPolicyModel?> = _privacyPolicy

    init {
        fetchPolicy()
    }

    private fun fetchPolicy() {
        viewModelScope.launch {
            _privacyPolicy.value = repo.getPrivacyPolicy()
        }
    }
}
