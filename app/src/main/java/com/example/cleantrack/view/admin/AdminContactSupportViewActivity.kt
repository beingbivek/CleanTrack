package com.example.cleantrack.view.admin

import ContactSupportRepoImpl
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Reply
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.cleantrack.model.ContactSupportModel
import com.example.cleantrack.ui.theme.*
import com.example.cleantrack.viewmodel.ContactSupportViewModel
import java.text.SimpleDateFormat
import java.util.*

class AdminContactSupportViewActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Initialize ViewModel with the Repo
        val viewModel = ContactSupportViewModel(ContactSupportRepoImpl())

        setContent {
            AdminDashboardScreen(viewModel) { finish() }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminDashboardScreen(viewModel: ContactSupportViewModel, onBack: () -> Unit) {
    val issues by viewModel.allIssues.observeAsState(emptyList())

    LaunchedEffect(Unit) {
        viewModel.fetchAllTickets()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Support Management", color = White) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = null, tint = White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Green)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(Color(0xFFF8F9FA)) // Slight off-white for contrast
        ) {
            if (issues.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No support tickets found.", color = Color.Gray)
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(issues) { issue ->
                        AdminIssueCard(
                            issue = issue,
                            viewModel = viewModel
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun AdminIssueCard(issue: ContactSupportModel, viewModel: ContactSupportViewModel) {
    var showReplyField by remember { mutableStateOf(false) }
    var replyText by remember { mutableStateOf("") }
    var statusExpanded by remember { mutableStateOf(false) }

    val statusColor = when (issue.status) {
        "OPEN" -> Color(0xFFE65100)
        "REPLIED" -> Color(0xFF0288D1)
        "CLOSED" -> Color(0xFF388E3C)
        else -> Color.Gray
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(4.dp),
        colors = CardDefaults.cardColors(containerColor = White)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header: User Details & Status Dropdown
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(issue.fullname, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Text(issue.email, fontSize = 12.sp, color = Color.Gray)
                }

                Box {
                    Surface(
                        color = statusColor.copy(alpha = 0.1f),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.clickable { statusExpanded = true }
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Text(issue.status, color = statusColor, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            Icon(Icons.Default.ArrowDropDown, null, tint = statusColor)
                        }
                    }

                    DropdownMenu(expanded = statusExpanded, onDismissRequest = { statusExpanded = false }) {
                        // Status Lock Logic: If not OPEN, don't show OPEN as an option
                        val availableStatuses = if (issue.status == "OPEN") {
                            listOf("OPEN", "REPLIED", "CLOSED")
                        } else {
                            listOf("REPLIED", "CLOSED")
                        }

                        availableStatuses.forEach { status ->
                            DropdownMenuItem(
                                text = { Text(status) },
                                onClick = {
                                    viewModel.changeStatus(issue, status)
                                    statusExpanded = false
                                }
                            )
                        }
                    }
                }
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), thickness = 0.5.dp)

            // Issue Content
            Text("Category: ${issue.category}", color = Green, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
            Spacer(modifier = Modifier.height(4.dp))
            Text(issue.message, fontSize = 14.sp, color = Black)

            // Admin Reply Bubble (The "Where do I see it" part)
            if (issue.adminReply.isNotEmpty()) {
                Spacer(modifier = Modifier.height(10.dp))
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFE8F5E9), RoundedCornerShape(8.dp))
                        .padding(10.dp)
                ) {
                    Text("Admin Reply:", fontWeight = FontWeight.Bold, color = Green, fontSize = 11.sp)
                    Text(text = issue.adminReply, fontSize = 13.sp, color = Black)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Metadata: Time & UserType
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(
                    text = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault()).format(Date(issue.timestamp)),
                    fontSize = 11.sp, color = Color.Gray
                )
                Text(
                    text = issue.userType,
                    fontSize = 11.sp, fontWeight = FontWeight.Bold,
                    color = if(issue.userType == "REGISTERED") Color(0xFF2E7D32) else Color.Gray
                )
            }

            // Reply Interface
            if (showReplyField) {
                OutlinedTextField(
                    value = replyText,
                    onValueChange = { replyText = it },
                    modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
                    placeholder = { Text("Type your response...") },
                    shape = RoundedCornerShape(8.dp)
                )
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = { showReplyField = false }) { Text("Cancel") }
                    Button(
                        onClick = {
                            if (replyText.isNotBlank()) {
                                viewModel.replyToTicket(issue, replyText)
                                showReplyField = false
                                replyText = ""
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Green)
                    ) {
                        Text("Send", color = White)
                    }
                }
            } else {
                // If already replied, show "Edit Reply" or just "Reply"
                TextButton(
                    onClick = { showReplyField = true },
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Icon(Icons.Default.Reply, contentDescription = null, modifier = Modifier.size(18.dp))
                    Text(if (issue.adminReply.isEmpty()) " Reply" else " Update Reply")
                }
            }
        }
    }
}