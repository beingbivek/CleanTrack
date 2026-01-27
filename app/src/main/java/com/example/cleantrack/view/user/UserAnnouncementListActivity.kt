package com.example.cleantrack.view.user

import android.os.Bundle
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
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.cleantrack.model.AnnouncementModel
import com.example.cleantrack.repository.AnnouncementRepoImpl
import com.example.cleantrack.ui.theme.*
import com.example.cleantrack.viewmodel.AnnouncementViewModel
import java.text.SimpleDateFormat
import java.util.*

class UserAnnouncementListActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AnnouncementListScreen()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnnouncementListScreen() {
    val context = LocalContext.current
    val announcementVM = remember { AnnouncementViewModel(AnnouncementRepoImpl()) }

    // FIX: Start loading as 'true' by default so there is no flicker
    val announcements by announcementVM.allAnnouncements.observeAsState(null)
    val isLoading by announcementVM.loading.observeAsState(true)

    LaunchedEffect(Unit) {
        announcementVM.getAllAnnouncements { _, _, _ -> }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(Blue, Green, Color.White),
                    startY = 0f,
                    endY = 1200f
                )
            )
    ) {
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                CenterAlignedTopAppBar(
                    title = {
                        Text("Announcements", fontWeight = FontWeight.ExtraBold, color = Color.White)
                    },
                    navigationIcon = {
                        IconButton(onClick = { (context as? ComponentActivity)?.finish() }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = Color.White)
                        }
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.Transparent)
                )
            }
        ) { padding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {
                // --- HIGHEST PRIORITY: LOADING ---
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center),
                        color = Green, // Green is visible on the bottom white gradient
                        strokeWidth = 4.dp
                    )
                }
                // --- DATA RECEIVED ---
                else {
                    if (announcements.isNullOrEmpty()) {
                        Text(
                            "No announcements yet.",
                            modifier = Modifier.align(Alignment.Center),
                            color = Color.White.copy(alpha = 0.8f),
                            fontWeight = FontWeight.Medium
                        )
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            items(announcements!!) { item ->
                                AnnouncementHistoryCard(item)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AnnouncementHistoryCard(announcement: AnnouncementModel) {
    val sdf = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault())
    val dateString = sdf.format(Date(announcement.timestamp))

    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = dateString,
            fontSize = 11.sp,
            color = Color(0xFF424242), // Dark Gray for better contrast
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(start = 8.dp, bottom = 6.dp)
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = White),
            elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
        ) {
            Row(
                modifier = Modifier.padding(20.dp),
                verticalAlignment = Alignment.Top
            ) {
                Surface(
                    shape = CircleShape,
                    color = Green.copy(alpha = 0.1f),
                    modifier = Modifier.size(45.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.Campaign, null, tint = Green, modifier = Modifier.size(24.dp))
                    }
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column {
                    Text(text = announcement.title, fontWeight = FontWeight.ExtraBold, color = Black, fontSize = 16.sp)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(text = announcement.message, color = Color.DarkGray, fontSize = 14.sp, lineHeight = 20.sp)
                }
            }
        }
    }
}