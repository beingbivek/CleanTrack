package com.example.cleantrack.viewmodel

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.cleantrack.model.AnnouncementModel
import com.example.cleantrack.repository.AnnouncementRepo

class AnnouncementViewModel(val repo : AnnouncementRepo) : ViewModel() {

    private val _allAnnouncements = MutableLiveData<List<AnnouncementModel>?>()
    val allAnnouncements : MutableLiveData<List<AnnouncementModel>?> get() = _allAnnouncements

    // 🔹 Added Loading State
    private val _loading = MutableLiveData<Boolean>()
    val loading: MutableLiveData<Boolean> get() = _loading

    fun postAnnouncement(model: AnnouncementModel, callback: (Boolean, String) -> Unit){
        repo.postAnnouncement(model, callback)
    }

    fun getAllAnnouncements(callback: (Boolean, String, List<AnnouncementModel>) -> Unit){
        _loading.value = true // Start loading
        repo.getAllAnnouncements { success, message, data ->
            if (success){
                _allAnnouncements.postValue(data)
            } else {
                Log.e("AnnouncementVM", "Error fetching announcements: $message")
                _allAnnouncements.postValue(emptyList())
            }
            _loading.postValue(false) // Stop loading
        }
    }

    fun deleteAnnouncement(id: String, callback: (Boolean, String) -> Unit){
        repo.deleteAnnouncement(id, callback)
    }

    fun editAnnouncement(id : String, model : AnnouncementModel, callback: (Boolean, String) -> Unit){
        repo.editAnnouncement(id, model, callback)
    }

    fun markAsSeen(id: String, userId: String) {
        repo.markAsSeen(id, userId)
    }
}