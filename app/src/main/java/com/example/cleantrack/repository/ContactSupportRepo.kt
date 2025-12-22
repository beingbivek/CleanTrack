package com.example.cleantrack.repository

import com.example.cleantrack.model.ContactSupportModel

interface ContactSupportRepo {

    fun submitIssue(
        model: ContactSupportModel,
        callback: (Boolean, String) -> Unit
    )

    fun getAllIssues(
        callback: (Boolean, String, List<ContactSupportModel>) -> Unit
    )
}
