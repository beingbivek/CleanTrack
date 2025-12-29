package com.example.cleantrack.view.user

import ContactSupportRepoImpl
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Reply
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.cleantrack.model.ContactSupportModel
import com.example.cleantrack.ui.theme.*
import com.example.cleantrack.viewmodel.ContactSupportViewModel
import com.google.firebase.auth.FirebaseAuth
import java.text.SimpleDateFormat
import java.util.*

class IssuesViewActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val viewModel = ContactSupportViewModel(ContactSupportRepoImpl())
        val currentUser = FirebaseAuth.getInstance().currentUser
        val isGuest = currentUser == null || currentUser.isAnonymous

        setContent {
            IssuesViewScreen(
                viewModel = viewModel,
                isGuest = isGuest,
                userId = currentUser?.uid ?: "",
                onBack = { finish() }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IssuesViewScreen(viewModel: ContactSupportViewModel, isGuest: Boolean, userId: String, onBack: () -> Unit) {
    val allIssues by viewModel.allIssues.observeAsState(emptyList())
    val userIssues = allIssues.filter { it.userId == userId }

    LaunchedEffect(Unit) { viewModel.fetchAllTickets() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("My Support Tickets", color = White) },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, null, tint = White) }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Green)
            )
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding).background(Color(0xFFF8F9FA))) {
            if (isGuest) {
                GuestView()
            } else {
                if (userIssues.isEmpty()) {
                    Text("No tickets found.", modifier = Modifier.align(Alignment.Center), color = Color.Gray)
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(userIssues) { issue -> UserIssueCard(issue, viewModel) }
                    }
                }
            }
        }
    }
}

@Composable
fun UserIssueCard(issue: ContactSupportModel, viewModel: ContactSupportViewModel) {
    var showReplyField by remember { mutableStateOf(false) }
    var userReplyText by remember { mutableStateOf("") }
    val context = LocalContext.current

    val statusColor = when (issue.status.uppercase()) {
        "OPEN" -> Color(0xFFE65100)      // Deep Orange
        "REPLIED" -> Color(0xFF0288D1)   // Light Blue
        "CLOSED" -> Color(0xFF388E3C)    // Material Green
        else -> Color.Gray
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            // --- HEADER: Category & Status ---
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = issue.category.uppercase(),
                    style = TextStyle(
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp,
                        color = Green
                    )
                )

                Surface(
                    color = statusColor.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Text(
                        text = issue.status,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                        style = TextStyle(
                            color = statusColor,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // --- MESSAGE BODY ---
            Text(
                text = "My Message:",
                style = TextStyle(fontSize = 11.sp, color = Color.Gray, fontWeight = FontWeight.Medium)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = issue.message,
                style = TextStyle(fontSize = 15.sp, color = Black, lineHeight = 20.sp)
            )

            // --- ADMIN REPLY SECTION (If exists) ---
            if (issue.adminReply.isNotEmpty()) {
                Spacer(modifier = Modifier.height(16.dp))
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFF1F8E9), RoundedCornerShape(12.dp)) // Very light green
                        .padding(12.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Email,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp),
                            tint = Green
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            "Support Response",
                            style = TextStyle(fontWeight = FontWeight.Bold, color = Green, fontSize = 12.sp)
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(text = issue.adminReply, fontSize = 14.sp, color = Black)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            Divider(color = Color.LightGray.copy(alpha = 0.5f), thickness = 0.5.dp)
            Spacer(modifier = Modifier.height(8.dp))

            // --- FOOTER: Date & Reply Button ---
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Updated: " + SimpleDateFormat("dd MMM, HH:mm", Locale.getDefault()).format(Date(issue.timestamp)),
                    style = TextStyle(fontSize = 11.sp, color = Color.Gray)
                )

                if (issue.status != "CLOSED") {
                    TextButton(
                        onClick = { showReplyField = !showReplyField },
                        contentPadding = PaddingValues(horizontal = 8.dp)
                    ) {
                        Icon(Icons.Default.Reply, null, modifier = Modifier.size(16.dp), tint = Green)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Reply Back", color = Green, fontWeight = FontWeight.Bold)
                    }
                }
            }

            // --- EXPANDABLE REPLY FIELD ---
            if (showReplyField) {
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = userReplyText,
                    onValueChange = { userReplyText = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Add more details...", fontSize = 14.sp) },
                    shape = RoundedCornerShape(12.dp),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = TextBoxColor,
                        unfocusedContainerColor = TextBoxColor,
                        focusedIndicatorColor = Green
                    )
                )
                Button(
                    onClick = {
                        if (userReplyText.isNotBlank()) {
                            viewModel.userReplyToTicket(issue, userReplyText) { success ->
                                if (success) {
                                    userReplyText = ""
                                    showReplyField = false
                                    Toast.makeText(context, "Update sent!", Toast.LENGTH_SHORT).show()
                                }
                            }
                        }
                    },
                    modifier = Modifier.align(Alignment.End).padding(top = 8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Green),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Send", color = White)
                }
            }
        }
    }
}

@Composable
fun GuestView() {
    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(Icons.Default.Email, null, modifier = Modifier.size(64.dp), tint = Green)
        Spacer(modifier = Modifier.height(16.dp))
        Text("Ticket Sent Successfully!", fontSize = 20.sp, fontWeight = FontWeight.Bold)
        Text("As a guest, check your email for replies from our team.", textAlign = TextAlign.Center, color = Color.Gray, modifier = Modifier.padding(top = 8.dp))
    }
}