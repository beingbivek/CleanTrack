package com.example.cleantrack.view.admin

import ContactSupportRepoImpl
import android.app.Activity
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.cleantrack.model.ContactSupportModel
import com.example.cleantrack.model.NotificationPayload
import com.example.cleantrack.repository.NotificationRepoImpl
import com.example.cleantrack.repository.UserRepoImpl
import com.example.cleantrack.ui.theme.*
import com.example.cleantrack.viewmodel.ContactSupportViewModel
import com.example.cleantrack.viewmodel.NotificationViewModel

class AdminContactSupportViewActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val viewModel = ContactSupportViewModel(ContactSupportRepoImpl())
        setContent {
            AdminDashboardScreen(viewModel) { finish() }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminDashboardScreen(viewModel: ContactSupportViewModel, onBack: () -> Unit) {
    val issues by viewModel.allIssues.observeAsState(null) // Initialize as null to track initial fetch
    val isLoading by viewModel.loading.observeAsState(false)
    val notificationViewModel = remember { NotificationViewModel(NotificationRepoImpl(), UserRepoImpl()) }

    var showFilters by remember { mutableStateOf(false) }
    var selectedUserType by remember { mutableStateOf("All") }
    var selectedCategory by remember { mutableStateOf("All") }
    var selectedStatus by remember { mutableStateOf("All") }

    val categories = listOf("All", "App & Technical Issues", "Login & Technical Issues", "Service-Related Issues", "Payments & Billing", "Account & Profile", "Location & Map", "Feedback & Others")
    val userTypes = listOf("All", "Registered", "Guest")
    val statuses = listOf("All", "OPEN", "REPLIED", "CLOSED")

    LaunchedEffect(Unit) {
        viewModel.fetchAllTickets()
    }

    val filteredIssues = issues?.filter { issue ->
        val matchesUserType = if (selectedUserType == "All") true
        else issue.userType.trim().equals(selectedUserType, ignoreCase = true)

        val matchesCategory = if (selectedCategory == "All") true
        else issue.category == selectedCategory

        val matchesStatus = if (selectedStatus == "All") true
        else issue.status.uppercase() == selectedStatus.uppercase()

        matchesUserType && matchesCategory && matchesStatus
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(Blue, Green, White), endY = 1100f))
    ) {
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                CenterAlignedTopAppBar(
                    title = { Text("Support Management", fontWeight = FontWeight.ExtraBold, color = White) },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = White)
                        }
                    },
                    actions = {
                        IconButton(onClick = { showFilters = !showFilters }) {
                            Icon(
                                imageVector = if (showFilters) Icons.Default.FilterListOff else Icons.Default.FilterList,
                                contentDescription = "Filter",
                                tint = White
                            )
                        }
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.Transparent)
                )
            }
        ) { padding ->
            // --- CLEAN LOADING LOGIC ---
            if (isLoading || issues == null) {
                Box(
                    modifier = Modifier.fillMaxSize().padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = Green, strokeWidth = 4.dp)
                }
            } else {
                Column(
                    modifier = Modifier.fillMaxSize().padding(padding)
                ) {
                    // --- EXPANDABLE FILTER PANEL ---
                    AnimatedVisibility(
                        visible = showFilters,
                        enter = expandVertically(),
                        exit = shrinkVertically()
                    ) {
                        Card(
                            modifier = Modifier.fillMaxWidth().padding(16.dp),
                            colors = CardDefaults.cardColors(containerColor = White),
                            shape = RoundedCornerShape(16.dp),
                            elevation = CardDefaults.cardElevation(4.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                FilterSection("Status", statuses, selectedStatus) { selectedStatus = it }
                                Spacer(modifier = Modifier.height(8.dp))
                                FilterSection("User Type", userTypes, selectedUserType) { selectedUserType = it }
                                Spacer(modifier = Modifier.height(8.dp))
                                FilterSection("Category", categories, selectedCategory) { selectedCategory = it }
                            }
                        }
                    }

                    // --- LIST SECTION ---
                    if (filteredIssues.isNullOrEmpty()) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text("No tickets found.", color = Color.DarkGray, fontWeight = FontWeight.Medium)
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(filteredIssues) { issue ->
                                AdminIssueCard(issue = issue, viewModel = viewModel, notificationViewModel)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun FilterSection(label: String, items: List<String>, selectedItem: String, onSelect: (String) -> Unit) {
    Text(label, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Green)
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(vertical = 4.dp)
    ) {
        items(items) { item ->
            FilterChip(
                selected = selectedItem == item,
                onClick = { onSelect(item) },
                label = { Text(item, fontSize = 12.sp) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = Green,
                    selectedLabelColor = White
                )
            )
        }
    }
}

@Composable
fun AdminIssueCard(issue: ContactSupportModel, viewModel: ContactSupportViewModel, notificationViewModel: NotificationViewModel) {
    var showReplyField by remember { mutableStateOf(false) }
    var adminReplyText by remember { mutableStateOf("") }
    var expandedStatus by remember { mutableStateOf(false) }

    val statusColor = when (issue.status.uppercase()) {
        "OPEN" -> Color(0xFFE65100)
        "REPLIED" -> Blue
        "CLOSED" -> Green
        else -> Color.Gray
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = White),
        elevation = CardDefaults.cardElevation(6.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(issue.fullname, fontWeight = FontWeight.ExtraBold, fontSize = 18.sp, color = Black)
                    Text(issue.email, fontSize = 13.sp, color = Color.Gray)
                }

                Surface(
                    color = statusColor.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.clickable { expandedStatus = true }
                ) {
                    Row(modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
                        Text(issue.status, color = statusColor, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        Icon(Icons.Default.ArrowDropDown, null, tint = statusColor, modifier = Modifier.size(16.dp))
                    }
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
            Text(issue.category.uppercase(), fontSize = 11.sp, color = Green, fontWeight = FontWeight.Black, letterSpacing = 1.sp)

            Spacer(modifier = Modifier.height(8.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFF8F9FA), RoundedCornerShape(16.dp))
                    .padding(16.dp)
            ) {
                com.example.cleantrack.util.StylizedConversation(message = issue.message)
            }

            if (issue.attachmentUrl.isNotEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                AsyncImage(
                    model = issue.attachmentUrl,
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color.LightGray),
                    contentScale = ContentScale.Crop
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            if (showReplyField) {
                OutlinedTextField(
                    value = adminReplyText,
                    onValueChange = { adminReplyText = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Write your response...") },
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Green)
                )
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = { showReplyField = false }) { Text("Cancel", color = Color.Gray) }
                    Button(
                        onClick = {
                            if (adminReplyText.isNotBlank()) {
                                viewModel.adminReplyToTicket(issue, adminReplyText)
                                notificationViewModel.notifyUser(
                                    issue.userId,
                                    NotificationPayload(
                                        title = "Support reply",
                                        message = "Admin replied to your support ticket.",
                                        type = "support_ticket",
                                        actionType = "ticket_detail",
                                        ticketId = issue.ticketId
                                    )
                                )
                                adminReplyText = ""
                                showReplyField = false
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Green),
                        shape = RoundedCornerShape(12.dp)
                    ) { Text("Send Reply") }
                }
            } else {
                Button(
                    onClick = { showReplyField = true },
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Green),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.Reply, null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Respond to Ticket", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}