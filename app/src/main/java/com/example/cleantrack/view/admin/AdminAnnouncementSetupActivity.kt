package com.example.cleantrack.view.admin

import android.app.Activity
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.cleantrack.model.AnnouncementModel
import com.example.cleantrack.model.NotificationPayload
import com.example.cleantrack.repository.AnnouncementRepoImpl
import com.example.cleantrack.repository.NotificationRepoImpl
import com.example.cleantrack.repository.UserRepoImpl
import com.example.cleantrack.ui.theme.Green // Assuming your Primary Green is named Green or PrimaryGreen
import com.example.cleantrack.viewmodel.AnnouncementViewModel
import com.example.cleantrack.viewmodel.NotificationViewModel

class AdminAnnouncementSetupActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val announcementId = intent.getStringExtra("ANNOUNCEMENT_ID")

        setContent {
            AdminAnnouncementSetupScreen(announcementId)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminAnnouncementSetupScreen(announcementId: String?) {
    val context = LocalContext.current
    val activity = context as Activity

    val announcementVM = remember { AnnouncementViewModel(AnnouncementRepoImpl()) }
    val notificationVM = remember { NotificationViewModel(NotificationRepoImpl(), UserRepoImpl()) }

    // UI states
    var title by remember { mutableStateOf("") }
    var message by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("General") }

    // Loading for initial data fetch
    var isInitialLoading by remember { mutableStateOf(false) }
    // Loading for button click
    var isPosting by remember { mutableStateOf(false) }

    val categories = listOf("General", "Urgent", "Schedule", "Holiday")
    val allAnnouncements by announcementVM.allAnnouncements.observeAsState(emptyList())

    // 1. Load data if in Edit mode
    LaunchedEffect(Unit) {
        if (announcementId != null) {
            isInitialLoading = true
            announcementVM.getAllAnnouncements { _, _, _ ->
                isInitialLoading = false
            }
        }
    }

    // 2. Pre-fill data
    LaunchedEffect(allAnnouncements) {
        if (announcementId != null) {
            val existing = allAnnouncements?.find { it.id == announcementId }
            existing?.let {
                title = it.title
                message = it.message
                category = it.category
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(Green, Color.White),
                    startY = 0f,
                    endY = 1000f
                )
            )
    ) {
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                CenterAlignedTopAppBar(
                    title = {
                        Text(
                            if (announcementId == null) "New Announcement" else "Edit Announcement",
                            style = TextStyle(
                                color = Color.White,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 20.sp
                            )
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = { activity.finish() }) {
                            Icon(
                                Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                                tint = Color.White
                            )
                        }
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = Color.Transparent
                    )
                )
            }
        ) { padding ->
            if (isInitialLoading) {
                Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Green)
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .padding(horizontal = 20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    contentPadding = PaddingValues(top = 10.dp, bottom = 30.dp)
                ) {
                    item {
                        OutlinedTextField(
                            value = title,
                            onValueChange = { title = it },
                            modifier = Modifier.fillMaxWidth(),
                            label = { Text("Title") },
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Green,
                                focusedLabelColor = Green
                            )
                        )
                    }

                    item {
                        AnnouncementDropdown(
                            label = "Category",
                            value = category,
                            options = categories,
                            onSelect = { category = it }
                        )
                    }

                    item {
                        OutlinedTextField(
                            value = message,
                            onValueChange = { message = it },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(180.dp),
                            label = { Text("Message Body") },
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Green,
                                focusedLabelColor = Green
                            )
                        )
                    }

                    item {
                        Spacer(Modifier.height(8.dp))
                        Button(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp),
                            enabled = !isPosting,
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Green),
                            onClick = {
                                if (title.isBlank() || message.isBlank()) {
                                    Toast.makeText(context, "Please fill all fields", Toast.LENGTH_SHORT).show()
                                    return@Button
                                }

                                isPosting = true
                                val model = AnnouncementModel(
                                    id = announcementId ?: "",
                                    title = title,
                                    message = message,
                                    category = category,
                                    timestamp = System.currentTimeMillis()
                                )

                                if (announcementId == null) {
                                    announcementVM.postAnnouncement(model) { success, msg ->
                                        isPosting = false
                                        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                                        if (success) {
                                            notificationVM.notifyAllUsersAndDrivers(
                                                NotificationPayload(
                                                    title = "New announcement",
                                                    message = model.title,
                                                    type = "announcement",
                                                    actionType = "announcement"
                                                )
                                            )
                                            activity.finish()
                                        }
                                    }
                                } else {
                                    announcementVM.editAnnouncement(announcementId, model) { success, msg ->
                                        isPosting = false
                                        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                                        if (success) activity.finish()
                                    }
                                }
                            }
                        ) {
                            if (isPosting) {
                                CircularProgressIndicator(
                                    color = Color.White,
                                    modifier = Modifier.size(24.dp),
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Text(
                                    if (announcementId == null) "Post Announcement" else "Save Changes",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnnouncementDropdown(label: String, value: String, options: List<String>, onSelect: (String) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = !expanded }) {
        OutlinedTextField(
            value = value,
            onValueChange = {},
            readOnly = true,
            label = { Text(label) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor(),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Green,
                focusedLabelColor = Green
            )
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.background(Color.White)
        ) {
            options.forEach {
                DropdownMenuItem(
                    text = { Text(it) },
                    onClick = {
                        onSelect(it)
                        expanded = false
                    }
                )
            }
        }
    }
}