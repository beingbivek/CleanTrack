package com.example.cleantrack.model

import java.sql.Timestamp

data class PrivacyPolicyModel(
    val date : Timestamp,
    val description : String,
    val privacypolicyId : String,

)
