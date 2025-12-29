package com.example.cleantrack.view.admin

import ContactSupportRepoImpl
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.cleantrack.model.ContactSupportModel
import com.example.cleantrack.ui.theme.*
import com.example.cleantrack.viewmodel.ContactSupportViewModel

class AdminContactSupportViewActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
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

    // --- STATES ---
    var showFilters by remember { mutableStateOf(false) } // Toggle for filter panel
    var selectedUserType by remember { mutableStateOf("All") }
    var selectedCategory by remember { mutableStateOf("All") }
    var selectedStatus by remember { mutableStateOf("All") }

    val categories = listOf("All", "App & Technical Issues", "Login & Technical Issues", "Service-Related Issues", "Payments & Billing", "Account & Profile", "Location & Map", "Feedback & Others")
    val userTypes = listOf("All", "Registered", "Guest")
    val statuses = listOf("All", "OPEN", "REPLIED", "CLOSED")

    // Filter Logic
    val filteredIssues = issues.filter { issue ->
        val matchesUserType = if (selectedUserType == "All") true
        else issue.userType.trim().equals(selectedUserType, ignoreCase = true)

        val matchesCategory = if (selectedCategory == "All") true
        else issue.category == selectedCategory

        val matchesStatus = if (selectedStatus == "All") true
        else issue.status.uppercase() == selectedStatus.uppercase()

        matchesUserType && matchesCategory && matchesStatus
    }

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
                actions = {
                    // Filter Toggle Button
                    IconButton(onClick = { showFilters = !showFilters }) {
                        Icon(
                            imageVector = if (showFilters) Icons.Default.FilterListOff else Icons.Default.FilterList,
                            contentDescription = "Filter",
                            tint = White
                        )
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
                .background(Color(0xFFF8F9FA))
        ) {
            // --- EXPANDABLE FILTER PANEL ---
            AnimatedVisibility(
                visible = showFilters,
                enter = expandVertically(),
                exit = shrinkVertically()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(White)
                        .padding(bottom = 12.dp)
                ) {
                    Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                        // Status Row
                        Text("Status", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = Color.Gray)
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(statuses) { status ->
                                FilterChip(
                                    selected = selectedStatus == status,
                                    onClick = { selectedStatus = status },
                                    label = { Text(status, fontSize = 12.sp) }
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // User Type Row
                        Text("User Type", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = Color.Gray)
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            userTypes.forEach { type ->
                                FilterChip(
                                    selected = selectedUserType == type,
                                    onClick = { selectedUserType = type },
                                    label = { Text(type, fontSize = 12.sp) }
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Category Row
                        Text("Category", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = Color.Gray)
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(categories) { category ->
                                FilterChip(
                                    selected = selectedCategory == category,
                                    onClick = { selectedCategory = category },
                                    label = { Text(category, fontSize = 12.sp) }
                                )
                            }
                        }
                    }
                    HorizontalDivider(thickness = 1.dp, color = Color.LightGray.copy(alpha = 0.3f))
                }
            }

            // --- LIST SECTION ---
            if (filteredIssues.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No tickets found.", color = Color.Gray)
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(filteredIssues) { issue ->
                        AdminIssueCard(issue = issue, viewModel = viewModel)
                    }
                }
            }
        }
    }
}

@Composable
fun AdminIssueCard(issue: ContactSupportModel, viewModel: ContactSupportViewModel) {
    var showReplyField by remember { mutableStateOf(false) }
    var adminReplyText by remember { mutableStateOf("") }
    var expandedStatus by remember { mutableStateOf(false) }
    val context = LocalContext.current

    val statusColor = when (issue.status.uppercase()) {
        "OPEN" -> Color(0xFFE65100)
        "REPLIED" -> Color(0xFF0288D1)
        "CLOSED" -> Color(0xFF388E3C)
        else -> Color.Gray
    }

    Card(
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp, horizontal = 16.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = White),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            // Header with User Name and Status Toggle
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column {
                    Text(issue.fullname, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Text(issue.email, fontSize = 12.sp, color = Color.Gray)
                }

                // Status Picker
                Box {
                    Text(
                        text = issue.status,
                        color = statusColor,
                        modifier = Modifier
                            .background(statusColor.copy(0.1f), RoundedCornerShape(8.dp))
                            .clickable { expandedStatus = true }
                            .padding(horizontal = 12.dp, vertical = 4.dp),
                        style = TextStyle(fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    )
                    DropdownMenu(expanded = expandedStatus, onDismissRequest = { expandedStatus = false }) {
                        listOf("OPEN", "REPLIED", "CLOSED").forEach { s ->
                            DropdownMenuItem(text = { Text(s) }, onClick = {
                                viewModel.changeStatus(issue, s)
                                expandedStatus = false
                            })
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            Text("CATEGORY: ${issue.category}", fontSize = 11.sp, color = Green, fontWeight = FontWeight.Bold)

            Spacer(modifier = Modifier.height(8.dp))

            // --- CONVERSATION THREAD (The "Instagram Comment" Style) ---
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFF5F5F5), RoundedCornerShape(12.dp))
                    .padding(12.dp)
            ) {
                Text(
                    text = issue.message,
                    style = TextStyle(fontSize = 14.sp, color = Black, lineHeight = 20.sp)
                )
            }

            // Image attachment if it exists
            if (issue.attachmentUrl.isNotEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                AsyncImage(
                    model = issue.attachmentUrl,
                    contentDescription = null,
                    modifier = Modifier.fillMaxWidth().height(200.dp).clip(RoundedCornerShape(12.dp)),
                    contentScale = ContentScale.Crop
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Reply Section
            if (showReplyField) {
                OutlinedTextField(
                    value = adminReplyText,
                    onValueChange = { adminReplyText = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Write a reply to the user...") },
                    shape = RoundedCornerShape(12.dp)
                )
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = { showReplyField = false }) { Text("Cancel") }
                    Button(
                        onClick = {
                            if (adminReplyText.isNotBlank()) {
                                viewModel.adminReplyToTicket(issue, adminReplyText)
                                adminReplyText = ""
                                showReplyField = false
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Green)
                    ) {
                        Text("Send Reply", color = White)
                    }
                }
            } else {
                Button(
                    onClick = { showReplyField = true },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Green),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.Reply, null, tint = White)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Reply to Conversation", color = White)
                }
            }
        }
    }
}