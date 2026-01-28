package com.example.cleantrack.view.common

import android.app.Activity
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.cleantrack.model.LeaderboardModel
import com.example.cleantrack.repository.PointsRepoImpl
import com.example.cleantrack.viewmodel.LeaderboardViewModel
import com.google.firebase.auth.FirebaseAuth

class LeaderboardActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge() // Added to match your app's theme
        setContent {
            LeaderboardScreen()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LeaderboardScreen() {
    val context = LocalContext.current
    val viewModel = remember { LeaderboardViewModel(PointsRepoImpl()) }
    val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: ""
    val users by viewModel.leaderboardState
    val loading by viewModel.isLoading

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(Color(0xFF4CAF50), Color(0xFF81C784), Color.White),
                    endY = 1000f
                )
            )
    ) {
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                CenterAlignedTopAppBar(
                    title = { Text("Leaderboard", fontWeight = FontWeight.ExtraBold, color = Color.White) },
                    navigationIcon = {
                        IconButton(onClick = { (context as? Activity)?.finish() }) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                                tint = Color.White
                            )
                        }
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.Transparent)
                )
            }
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {
                // --- USER RANK SUMMARY SECTION ---
                val currentUser = users.find { it.userId == currentUserId }
                val currentRank = if (currentUser != null) users.indexOf(currentUser) + 1 else 0

                Column(modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color.White.copy(alpha = 0.15f))
                            .padding(16.dp)
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = Color.White.copy(alpha = 0.2f),
                            modifier = Modifier.size(50.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.EmojiEvents, null, tint = Color.White, modifier = Modifier.size(30.dp))
                            }
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Column {
                            Text(
                                text = "Your Current Rank: ${if (currentRank > 0) "${currentRank}th" else "N/A"}",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                            Text(
                                text = "Total Points: ${currentUser?.points ?: 0}",
                                color = Color.White.copy(0.8f),
                                fontSize = 14.sp
                            )
                        }
                    }
                }

                // --- RANKING LIST SECTION ---
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp),
                    color = Color.White,
                    tonalElevation = 2.dp
                ) {
                    if (loading) {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(color = Color(0xFF4CAF50))
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(20.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            itemsIndexed(users) { index, entry ->
                                LeaderboardRow(index + 1, entry, entry.userId == currentUserId)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun LeaderboardRow(rank: Int, entry: LeaderboardModel, isMe: Boolean) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isMe) Color(0xFFF1F8E9) else Color(0xFFF8F9FA)
        ),
        elevation = CardDefaults.cardElevation(if (isMe) 2.dp else 0.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Rank / Trophy
            Box(Modifier.width(40.dp), contentAlignment = Alignment.Center) {
                when (rank) {
                    1 -> Icon(Icons.Default.EmojiEvents, "Gold", tint = Color(0xFFFFD700), modifier = Modifier.size(28.dp))
                    2 -> Icon(Icons.Default.EmojiEvents, "Silver", tint = Color(0xFFC0C0C0), modifier = Modifier.size(26.dp))
                    3 -> Icon(Icons.Default.EmojiEvents, "Bronze", tint = Color(0xFFCD7F32), modifier = Modifier.size(24.dp))
                    else -> Text("$rank", fontWeight = FontWeight.ExtraBold, color = Color.Gray)
                }
            }

            Spacer(Modifier.width(8.dp))

            // Avatar Placeholder
            Surface(
                modifier = Modifier.size(45.dp),
                shape = CircleShape,
                color = if (isMe) Color(0xFF4CAF50).copy(0.2f) else Color.LightGray.copy(0.3f)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        Icons.Default.Person,
                        null,
                        tint = if (isMe) Color(0xFF4CAF50) else Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            Spacer(Modifier.width(12.dp))

            // Name
            Text(
                text = entry.fullname,
                modifier = Modifier.weight(1f),
                fontWeight = if (isMe) FontWeight.ExtraBold else FontWeight.Bold,
                color = if (isMe) Color(0xFF2E7D32) else Color.Black,
                fontSize = 15.sp
            )

            // Points
            Text(
                text = "${entry.points} pts",
                color = Color(0xFF4CAF50),
                fontWeight = FontWeight.Black,
                fontSize = 14.sp
            )
        }
    }
}