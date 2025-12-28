package com.example.cleantrack.model

data class AnnouncementModel(
    val id: String = "",
    val title: String = "",
    val message: String = "",
    val category: String = "General",
    val timestamp: Long = System.currentTimeMillis()
)
