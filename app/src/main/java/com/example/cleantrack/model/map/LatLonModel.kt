package com.example.cleantrack.model.map

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class LatLonModel(
    val lat: Double,
    val lon: Double
) : Parcelable