package com.example.cleantrack.repository

import com.example.cleantrack.model.AnnouncementModel
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

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
        ref.addValueEventListener(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                val list = mutableListOf<AnnouncementModel>()
                snapshot.children.forEach { list.add(it.getValue(AnnouncementModel::class.java)!!) }
                callback(true, "Success", list.reversed()) // Newest first
            }

            override fun onCancelled(error: DatabaseError) {
                callback(false, error.message, emptyList())
            }
        })
    }

    override fun deleteAnnouncement(
        id: String,
        callback: (Boolean, String) -> Unit
    ) {
        ref.child(id).removeValue().addOnCompleteListener {
            if (it.isSuccessful) callback(true, "Announcement Deleted") else callback(false, "Failed")
        }
    }

    override fun editAnnouncement(
        id: String,
        model: AnnouncementModel,
        callback: (Boolean, String) -> Unit
    ) {
        ref.child(id).updateChildren(model.toMap())
            .addOnCompleteListener {
                if (it.isSuccessful){
                    callback(true, "Announcement updated")
                }else{
                    callback(false, "${it.exception?.message}")
                }
            }
    }

    override fun markAsSeen(id: String, userId: String) {
        // We update only the specific child to avoid overwriting other data
        ref.child(id).child("seenBy").child(userId).setValue(true)
    }
}