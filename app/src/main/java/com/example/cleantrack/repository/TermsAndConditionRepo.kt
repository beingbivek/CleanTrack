package com.example.cleantrack.repository

interface TermsAndConditionRepo {
    fun updateTermsAndCondition(content: String, callback: (Boolean, String) -> Unit)
    fun getTermsAndCondition(callback: (Boolean, String, String?) -> Unit)
}