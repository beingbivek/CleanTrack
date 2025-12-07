package com.example.cleantrack.model

import java.sql.Timestamp

data class PrivacyPolicyModel(
    val privacypolicyId : String,
    val date : Timestamp,
    val description : String
)
