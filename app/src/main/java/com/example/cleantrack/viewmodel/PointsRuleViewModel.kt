package com.example.cleantrack.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.cleantrack.model.PointsRuleModel
import com.example.cleantrack.repository.PointsRuleRepo

class PointsRuleViewModel(
    private val repo: PointsRuleRepo
) : ViewModel() {

    val rules = MutableLiveData<List<PointsRuleModel>?>()
    val loading = MutableLiveData(false)

    fun loadRules() {
        loading.postValue(true)
        repo.getAllRules { success, _, data ->
            if (success) rules.postValue(data)
            loading.postValue(false)
        }
    }

    fun addRule(model: PointsRuleModel, callback: (Boolean, String) -> Unit) {
        repo.addRule(model, callback)
    }

    fun updateRule(model: PointsRuleModel, callback: (Boolean, String) -> Unit) {
        repo.updateRule(model, callback)
    }

    fun deleteRule(ruleId: String, callback: (Boolean, String) -> Unit) {
        repo.deleteRule(ruleId, callback)
    }
}
