package com.example.cleantrack.view.admin

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.cleantrack.model.AnnouncementModel
import com.example.cleantrack.repository.AnnouncementRepoImpl
import com.example.cleantrack.ui.theme.Green
import com.example.cleantrack.ui.theme.White
import com.example.cleantrack.viewmodel.AnnouncementViewModel

class AdminAnnouncementListActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {

            AdminAnnouncementListScreen()

        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminAnnouncementListScreen() {
    val context = LocalContext.current
    val announcementVM = remember { AnnouncementViewModel(AnnouncementRepoImpl()) }
    val announcements by announcementVM.allAnnouncements.observeAsState(emptyList())

    var announcementIdToDelete by remember { mutableStateOf<String?>(null) }

    // Load announcements on entry
    LaunchedEffect(Unit) {
        announcementVM.getAllAnnouncements { _, _, _ -> }
    }

    // --- ALERT DIALOG LOGIC ---
    if (announcementIdToDelete != null) {
        AlertDialog(
            onDismissRequest = { announcementIdToDelete = null },
            title = { Text("Delete Announcement") },
            text = { Text("Are you sure you want to delete this announcement? This cannot be undone.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        // Safe call using the stored ID
                        announcementIdToDelete?.let { id ->
                            announcementVM.deleteAnnouncement(id) { success, msg ->
                                Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                            }
                        }
                        announcementIdToDelete = null // Hide dialog
                    }
                ) {
                    Text("Delete", color = Color.Red)
                }
            },
            dismissButton = {
                TextButton(onClick = { announcementIdToDelete = null }) {
                    Text("Cancel")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Manage Announcements", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { (context as? ComponentActivity)?.finish() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = null)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = White)
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    val intent = Intent(context, AdminAnnouncementSetupActivity::class.java)
                    context.startActivity(intent)
                },
                containerColor = Green,
                contentColor = Color.White
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add New")
            }
        }
    ) { padding ->
        if (announcements.isNullOrEmpty()) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text("No announcements found.", color = Color.Gray)
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .background(Color(0xFFF5F5F5)),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(announcements!!) { announcement ->
                    AnnouncementItem(
                        announcement = announcement,
                        onEdit = {
                            val intent = Intent(context, AdminAnnouncementSetupActivity::class.java)
                            intent.putExtra("ANNOUNCEMENT_ID", announcement.id)
                            context.startActivity(intent)
                        },
                        onDelete = {
                            announcementIdToDelete = announcement.id
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun AnnouncementItem(
    announcement: AnnouncementModel,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = announcement.category.uppercase(),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = Green
                )
                Text(
                    text = announcement.title,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = announcement.message,
                    fontSize = 14.sp,
                    color = Color.Gray,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Row {
                IconButton(onClick = onEdit) {
                    Icon(Icons.Default.Edit, contentDescription = "Edit", tint = Color.Blue)
                }
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color.Red)
                }
            }
        }
    }
}
