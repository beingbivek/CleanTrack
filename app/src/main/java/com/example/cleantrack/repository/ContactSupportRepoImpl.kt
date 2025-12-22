package com.example.cleantrack.repository

import android.util.Log
import com.example.cleantrack.model.ContactSupportModel
import com.google.firebase.database.*

class ContactSupportRepoImpl : ContactSupportRepo {

    private val database: FirebaseDatabase = FirebaseDatabase.getInstance()
    private val ref: DatabaseReference = database.getReference("ContactSupport")

    override fun submitIssue(
        model: ContactSupportModel,
        callback: (Boolean, String) -> Unit
    ) {
        val issueId = ref.push().key ?: return

        val updatedModel = model.copy(issueId = issueId)

        ref.child(issueId)
            .setValue(updatedModel)
            .addOnSuccessListener {
                callback(true, "Issue submitted successfully")
            }
            .addOnFailureListener {
                callback(false, it.localizedMessage ?: "Failed to submit issue")
            }
    }

    override fun getAllIssues(
        callback: (Boolean, String, List<ContactSupportModel>) -> Unit
    ) {
        ref.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val list = mutableListOf<ContactSupportModel>()
                for (child in snapshot.children) {
                    val issue = child.getValue(ContactSupportModel::class.java)
                    if (issue != null) list.add(issue)
                }
                callback(true, "Issues fetched", list)
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("ContactSupportRepo", error.message)
                callback(false, error.message, emptyList())
            }
        })
    }
}
