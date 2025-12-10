package com.example.cleantrack.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class GeometryModel(
    val type: String,
    val coordinates: Double?
) : Parcelable
