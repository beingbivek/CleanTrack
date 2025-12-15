package com.example.cleantrack.model

data class PlaceAPIResponseModel(
    val timestamp: String,
    val status: Int,
    val message: String,
    val data: List<PlaceModel>
)
