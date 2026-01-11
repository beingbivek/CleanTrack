package com.example.cleantrack.model

data class TripHistoryUiModel(
    val trip: ActiveTripModel,
    val totalBins: Int,
    val collectedBins: Int,
    val driverName: String = ""
)