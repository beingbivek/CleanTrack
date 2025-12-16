package com.example.cleantrack.model.map

import com.example.cleantrack.model.map.PlaceModel

data class PlaceAPIResponseModel(
    val timestamp: String,
    val status: Int,
    val message: String,
    val data: List<PlaceModel>
)