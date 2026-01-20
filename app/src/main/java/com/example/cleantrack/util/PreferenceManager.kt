package com.example.cleantrack.util

import android.content.Context
import com.example.cleantrack.model.ScheduleModel
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class PreferenceManager(context: Context) {
    private val sharedPref = context.getSharedPreferences("CleanTrackPrefs", Context.MODE_PRIVATE)
    private val gson = Gson()

    fun setProUser(isPro: Boolean) {
        sharedPref.edit().putBoolean("is_pro", isPro).apply()
    }

    fun isProUser(): Boolean = sharedPref.getBoolean("is_pro", false)

    fun saveSchedules(schedules: List<ScheduleModel>) {
        val json = gson.toJson(schedules)
        sharedPref.edit().putString("offline_schedules", json).apply()
    }

    fun getSavedSchedules(): List<ScheduleModel> {
        val json = sharedPref.getString("offline_schedules", null) ?: return emptyList()
        val type = object : TypeToken<List<ScheduleModel>>() {}.type
        return gson.fromJson(json, type)
    }

    fun clearData() {
        // Removes saved schedule and pro status on logout
        sharedPref.edit().remove("is_pro").remove("offline_schedules").apply()
    }
}