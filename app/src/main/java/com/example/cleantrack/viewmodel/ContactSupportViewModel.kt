package com.example.cleantrack.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.cleantrack.model.ContactSupportModel
import com.example.cleantrack.repository.ContactSupportRepo

class ContactSupportViewModel(
    private val repo: ContactSupportRepo
) : ViewModel() {

    val issues = MutableLiveData<List<ContactSupportModel>>()
    val loading = MutableLiveData(false)

    fun submitIssue(
        model: ContactSupportModel,
        callback: (Boolean, String) -> Unit
    ) {
        loading.postValue(true)
        repo.submitIssue(model) { success, message ->
            loading.postValue(false)
            callback(success, message)
        }
    }

    fun getAllIssues() {
        loading.postValue(true)
        repo.getAllIssues { success, _, data ->
            loading.postValue(false)
            if (success) {
                issues.postValue(data)
            }
        }
    }
}
