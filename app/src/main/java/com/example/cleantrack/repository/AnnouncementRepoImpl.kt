package com.example.cleantrack.repository

import com.example.cleantrack.model.AnnouncementModel
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class AnnouncementRepoImpl : AnnouncementRepo {

    val database : FirebaseDatabase = FirebaseDatabase.getInstance()

    val ref : DatabaseReference = database.getReference("Announcements")


    override fun postAnnouncement(
        model: AnnouncementModel,
        callback: (Boolean, String) -> Unit
    ) {
        val id = ref.push().key ?: ""
        val finalModel = model.copy(id = id)

        ref.child(id).setValue(finalModel).addOnCompleteListener {
            if (it.isSuccessful) callback(true, "Announcement Posted")
            else callback(false, it.exception?.message ?: "Error")
        }
    }

    override fun getAllAnnouncements(callback: (Boolean, String, List<AnnouncementModel>) -> Unit) {
        TODO("Not yet implemented")
    }

    override fun deleteAnnouncement(
        id: String,
        callback: (Boolean, String) -> Unit
    ) {
        TODO("Not yet implemented")
    }
}