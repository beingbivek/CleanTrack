package com.example.cleantrack.repository

import com.example.cleantrack.model.AnnouncementModel

interface AnnouncementRepo {

    fun postAnnouncement(model: AnnouncementModel, callback: (Boolean, String) -> Unit)

    fun getAllAnnouncements(callback: (Boolean, String, List<AnnouncementModel>) -> Unit)

    fun deleteAnnouncement(id: String, callback: (Boolean, String) -> Unit)

    fun editAnnouncement(id : String, model : AnnouncementModel, callback: (Boolean, String) -> Unit)

    fun markAsSeen(id: String, userId: String)


}