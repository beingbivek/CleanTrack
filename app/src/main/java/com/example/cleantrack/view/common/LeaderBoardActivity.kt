package com.example.cleantrack.view.common

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
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.cleantrack.model.LeaderBoardUser
import com.example.cleantrack.repository.PointsRepo
import com.example.cleantrack.repository.PointsRepoImpl
import com.example.cleantrack.ui.theme.Bronze
import com.example.cleantrack.ui.theme.Gold
import com.example.cleantrack.ui.theme.Green
import com.example.cleantrack.ui.theme.Silver
import com.example.cleantrack.view.auth.StartBody
import com.example.cleantrack.viewmodel.LeaderboardViewModel
import com.google.firebase.auth.FirebaseAuth
import kotlin.collections.find

class LeaderboardActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val viewModel = LeaderboardViewModel(PointsRepoImpl())
        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: ""

        setContent {
            LeaderboardScreen(viewModel, currentUserId)
        }
    }
}

@Composable
fun LeaderboardScreen(viewModel: LeaderboardViewModel, currentUserId: String) {
    val users by viewModel.leaderboardState
    val loading by viewModel.isLoading

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(Color(0xFF4CAF50), Color(0xFF81C784), Color.White)))
    ) {
        Column(modifier = Modifier.fillMaxSize()) {

            // --- HEADER SECTION ---
            val currentUser = users.find { it.userId == currentUserId }
            val currentRank = if (currentUser != null) users.indexOf(currentUser) + 1 else 0

            Column(modifier = Modifier.padding(24.dp).padding(top = 16.dp)) {
                Text("Leaderboard", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = Color.White)
                Spacer(modifier = Modifier.height(12.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.EmojiEvents, null, tint = Color.White, modifier = Modifier.size(40.dp))
                    Column(Modifier.padding(start = 12.dp)) {
                        Text("Your Rank: ${if(currentRank > 0) "${currentRank}th" else "N/A"}",
                            color = Color.White, fontWeight = FontWeight.Bold)
                        Text("Points: ${currentUser?.points ?: 0}", color = Color.White.copy(0.8f))
                    }
                }
            }

            // --- LIST SECTION ---
            Surface(
                modifier = Modifier.fillMaxSize(),
                shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp),
                color = Color.White
            ) {
                if (loading) {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = Color(0xFF4CAF50))
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.padding(20.dp),
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

@Composable
fun LeaderboardRow(rank: Int, entry: LeaderBoardUser, isMe: Boolean) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(if (isMe) Color(0xFFF1F8E9) else Color.Transparent)
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Rank / Trophy
        Box(Modifier.width(35.dp), contentAlignment = Alignment.Center) {
            when (rank) {
                1 -> Icon(Icons.Default.EmojiEvents, "Gold", tint = Color(0xFFFFD700))
                2 -> Icon(Icons.Default.EmojiEvents, "Silver", tint = Color(0xFFC0C0C0))
                3 -> Icon(Icons.Default.EmojiEvents, "Bronze", tint = Color(0xFFCD7F32))
                else -> Text("$rank.", fontWeight = FontWeight.Bold)
            }
        }

        Spacer(Modifier.width(8.dp))

        // Avatar
        Box(Modifier.size(45.dp).clip(CircleShape).background(Color.LightGray), contentAlignment = Alignment.Center) {
            Icon(Icons.Default.Person, null, tint = Color.White)
        }

        Spacer(Modifier.width(12.dp))

        // Name
        Text(entry.fullname, modifier = Modifier.weight(1f),
            fontWeight = if(isMe) FontWeight.Bold else FontWeight.Medium)

        // Points
        Text("${entry.points} Pts", color = Color(0xFF4CAF50), fontWeight = FontWeight.ExtraBold)
    }
}

