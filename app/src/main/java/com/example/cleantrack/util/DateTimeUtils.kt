package com.example.cleantrack.util

import java.text.SimpleDateFormat
import java.util.*

object DateTimeUtils {
    fun isWithinSchedule(startTime: String, endTime: String): Boolean {
        val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
        val now = Calendar.getInstance()
        val currentTimeStr = sdf.format(now.time)

        val current = sdf.parse(currentTimeStr)
        val start = sdf.parse(startTime)
        val end = sdf.parse(endTime)

        return if (current != null && start != null && end != null) {
            current.after(start) && current.before(end)
        } else false
    }
}