package com.example.cleantrack.view.user

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.cleantrack.repository.AnnouncementRepoImpl
import com.example.cleantrack.ui.theme.White
import com.example.cleantrack.view.common.AnnouncementBanner
import com.example.cleantrack.viewmodel.AnnouncementViewModel

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
    val announcements by announcementVM.allAnnouncements.observeAsState(emptyList())

    LaunchedEffect(Unit) {
        announcementVM.getAllAnnouncements { _, _, _ -> }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Announcements", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { (context as? ComponentActivity)?.finish() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = White)
            )
        }
    ) { padding ->
        if (announcements.isNullOrEmpty()) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text("No announcements yet.", color = Color.Gray)
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .background(Color(0xFFF8F8F8)),
                contentPadding = PaddingValues(bottom = 16.dp)
            ) {
                // Since your Repo already does list.reversed(),
                // the first item is already the latest.
                items(announcements!!) { item ->
                    // Reuse the banner design but without the dismiss "X" button
                    // Or create a simpler card for the history list
                    AnnouncementHistoryCard(item)
                }
            }
        }
    }
}

@Composable
fun AnnouncementHistoryCard(announcement: com.example.cleantrack.model.AnnouncementModel) {
    // Reusing your design but removing the "onDismiss" logic for the list view
    AnnouncementBanner(announcement = announcement, onDismiss = {})
}