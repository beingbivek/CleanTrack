package com.example.cleantrack.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class TermsAndConditionModel(
    val content: String = "",
    val lastUpdated: Long = System.currentTimeMillis()
) : Parcelable