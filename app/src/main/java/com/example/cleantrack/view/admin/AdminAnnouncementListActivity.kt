package com.example.cleantrack.view.admin

import android.app.Activity
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.compose.ui.platform.LocalLifecycleOwner
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
    val activity = context as? Activity
    val lifecycleOwner = LocalLifecycleOwner.current

    val announcementVM = remember { AnnouncementViewModel(AnnouncementRepoImpl()) }
    // Observe the LiveData from ViewModel
    val announcements by announcementVM.allAnnouncements.observeAsState(null)

    var announcementIdToDelete by remember { mutableStateOf<String?>(null) }
    var isFetching by remember { mutableStateOf(true) }

    // Logic to refresh data when the activity is resumed (back from Edit/Add)
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                announcementVM.getAllAnnouncements { _, _, _ ->
                    isFetching = false
                }
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    // --- DELETE DIALOG ---
    if (announcementIdToDelete != null) {
        AlertDialog(
            onDismissRequest = { announcementIdToDelete = null },
            shape = RoundedCornerShape(24.dp),
            title = { Text("Delete Announcement", fontWeight = FontWeight.Bold) },
            text = { Text("Are you sure you want to delete this announcement? This action is permanent.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        announcementIdToDelete?.let { id ->
                            announcementVM.deleteAnnouncement(id) { success, msg ->
                                Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                                announcementVM.getAllAnnouncements { _, _, _ -> }
                            }
                        }
                        announcementIdToDelete = null
                    }
                ) {
                    Text("Delete", color = Color.Red, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { announcementIdToDelete = null }) {
                    Text("Cancel", color = Color.Gray)
                }
            }
        )
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
                            "Announcements",
                            style = TextStyle(color = Color.White, fontWeight = FontWeight.ExtraBold, fontSize = 20.sp)
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = { activity?.finish() }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = Color.White)
                        }
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.Transparent)
                )
            },
            floatingActionButton = {
                FloatingActionButton(
                    onClick = {
                        context.startActivity(Intent(context, AdminAnnouncementSetupActivity::class.java))
                    },
                    containerColor = Green,
                    contentColor = Color.White,
                    shape = CircleShape
                ) {
                    Icon(Icons.Default.Add, null)
                }
            }
        ) { padding ->
            Box(modifier = Modifier.fillMaxSize().padding(padding)) {
                // Keep loading if isFetching is true OR announcements hasn't been populated yet
                if (isFetching && announcements == null) {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = Green, strokeWidth = 4.dp)
                    }
                } else if (announcements.isNullOrEmpty()) {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("No announcements found.", color = Color.DarkGray, fontWeight = FontWeight.Medium)
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
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
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier.size(40.dp).background(Green.copy(0.1f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Campaign, null, tint = Green, modifier = Modifier.size(24.dp))
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = announcement.category.uppercase(),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Green
                )
                Text(
                    text = announcement.title,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = announcement.message,
                    fontSize = 13.sp,
                    color = Color.DarkGray,
                    maxLines = 2,
                    lineHeight = 18.sp
                )
            }
            Row {
                IconButton(onClick = onEdit) { Icon(Icons.Default.Edit, "Edit", tint = Green) }
                IconButton(onClick = onDelete) { Icon(Icons.Default.Delete, "Delete", tint = Color.Red) }
            }
        }
    }
}