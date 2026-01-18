import com.example.cleantrack.model.ContactSupportModel
import com.example.cleantrack.model.UserModel
import com.example.cleantrack.repository.ContactSupportRepo
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class ContactSupportRepoImpl : ContactSupportRepo {
    private val database = FirebaseDatabase.getInstance()
    private val issuesRef = database.getReference("Issues")
    private val usersRef = database.getReference("Users")
    private val auth = FirebaseAuth.getInstance()

    override fun submitIssue(model: ContactSupportModel, callback: (Boolean, String) -> Unit) {
        val categoryRef = issuesRef.child(model.category)
        val ticketId = categoryRef.push().key ?: return callback(false, "ID Error")
        val finalModel = model.copy(ticketId = ticketId)

        categoryRef.child(ticketId).setValue(finalModel).addOnCompleteListener {
            if (it.isSuccessful) callback(true, "Ticket Submitted")
            else callback(false, it.exception?.message ?: "Error")
        }
    }

    override fun getCurrentUserDetails(callback: (UserModel?) -> Unit) {
        val uid = auth.currentUser?.uid ?: return callback(null)
        usersRef.child(uid).get().addOnSuccessListener {
            callback(it.getValue(UserModel::class.java))
        }.addOnFailureListener { callback(null) }
    }

    override fun getAllIssues(callback: (Boolean, String, List<ContactSupportModel>) -> Unit) {
        issuesRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val list = mutableListOf<ContactSupportModel>()
                for (category in snapshot.children) {
                    for (ticket in category.children) {
                        ticket.getValue(ContactSupportModel::class.java)?.let { list.add(it) }
                    }
                }
                callback(true, "Success", list.sortedByDescending { it.timestamp })
            }
            override fun onCancelled(error: DatabaseError) { callback(false, error.message, emptyList()) }
        })
    }

    override fun updateTicketStatus(ticketId: String, category: String, newStatus: String, callback: (Boolean, String) -> Unit) {
        issuesRef.child(category).child(ticketId).child("status").setValue(newStatus)
            .addOnCompleteListener { callback(it.isSuccessful, if(it.isSuccessful) "Updated" else "Failed") }
    }

    override fun sendAdminReply(model: ContactSupportModel, callback: (Boolean) -> Unit) {
        issuesRef.child(model.category)
            .child(model.ticketId)
            .setValue(model)
            .addOnCompleteListener { callback(it.isSuccessful) }
    }

    override fun updateIssueThread(issue: ContactSupportModel, callback: (Boolean) -> Unit) {
        issuesRef.child(issue.category).child(issue.ticketId)
            .setValue(issue)
            .addOnCompleteListener { task ->
                callback(task.isSuccessful)
            }
    }
}