package com.example.cleantrack.view.admin

import android.app.Activity
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.cleantrack.model.AnnouncementModel
import com.example.cleantrack.repository.AnnouncementRepoImpl
import com.example.cleantrack.viewmodel.AnnouncementViewModel

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

    // UI states
    var title by remember { mutableStateOf("") }
    var message by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("General") }
    var isLoading by remember { mutableStateOf(false) }

    val categories = listOf("General", "Urgent", "Schedule", "Holiday")

    // Observe all announcements to find the one we need to edit
    val allAnnouncements by announcementVM.allAnnouncements.observeAsState(emptyList())

    // 1. Load data if in Edit mode
    LaunchedEffect(Unit) {
        if (announcementId != null) {
            isLoading = false
            announcementVM.getAllAnnouncements { _, _, _ ->
                isLoading = false
            }
        }
    }

    // 2. Pre-fill data when found in the list
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

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (announcementId == null) "New Announcement" else "Edit Announcement") },
                navigationIcon = {
                    IconButton(onClick = { activity.finish() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = null)
                    }
                }
            )
        }
    ) { padding ->
        if (isLoading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Title") }
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
                        modifier = Modifier.fillMaxWidth().height(150.dp),
                        label = { Text("Message Body") }
                    )
                }

                item {
                    Button(
                        modifier = Modifier.fillMaxWidth().height(55.dp),
                        onClick = {
                            if (title.isBlank() || message.isBlank()) {
                                Toast.makeText(context, "Please fill all fields", Toast.LENGTH_SHORT).show()
                                return@Button
                            }

                            val model = AnnouncementModel(
                                id = announcementId ?: "",
                                title = title,
                                message = message,
                                category = category,
                                timestamp = System.currentTimeMillis()
                            )

                            if (announcementId == null) {
                                // CREATE MODE
                                announcementVM.postAnnouncement(model) { success, msg ->
                                    Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                                    if (success) activity.finish()
                                }
                            } else {
                                // EDIT MODE
                                announcementVM.editAnnouncement(announcementId, model) { success, msg ->
                                    Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                                    if (success) activity.finish()
                                }
                            }
                        }
                    ) {
                        Text(if (announcementId == null) "Post Announcement" else "Save Changes")
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
            value = value, onValueChange = {}, readOnly = true, label = { Text(label) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
            modifier = Modifier.fillMaxWidth().menuAnchor()
        )
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            options.forEach {
                DropdownMenuItem(text = { Text(it) }, onClick = { onSelect(it); expanded = false })
            }
        }
    }
}