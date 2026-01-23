package com.example.cleantrack.model 

data class PaymentInitiationModel(
    val paymentIntentId: String,
    val clientSecret: String
)