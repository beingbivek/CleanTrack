package com.example.cleantrack.viewmodel

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.cleantrack.model.AnnouncementModel
import com.example.cleantrack.model.UserModel
import com.example.cleantrack.repository.AnnouncementRepo

class AnnouncementViewModel(val repo : AnnouncementRepo) : ViewModel() {

    fun postAnnouncement(model: AnnouncementModel, callback: (Boolean, String) -> Unit){

        repo.postAnnouncement( model, callback)

    }

    private val _allAnnouncements = MutableLiveData<List<AnnouncementModel>?>()
    val allAnnouncements : MutableLiveData<List<AnnouncementModel>?>
        get() = _allAnnouncements

    fun getAllAnnouncements(callback: (Boolean, String, List<AnnouncementModel>) -> Unit){

        repo.getAllAnnouncements{
                success, message, data ->
            Log.d("checkpoint",success.toString())
            if (success){
                _allAnnouncements.postValue(data)


            }else {
                // Log the error message to debug connection issues
                println("Error fetching users: $message")
                _allAnnouncements.postValue(emptyList()) // Post empty list on failure

            }

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