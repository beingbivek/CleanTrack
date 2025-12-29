package com.example.cleantrack.model

data class AnnouncementModel(
    val id: String = "",
    val title: String = "",
    val message: String = "",
    val category: String = "General",
    val timestamp: Long = System.currentTimeMillis()
){
    fun toMap(): Map<String, Any> {
        return mapOf(
            "id" to id,
            "title" to title,
            "message" to message,
            "category" to category,
            "timestamp" to timestamp
        )
    }
}
