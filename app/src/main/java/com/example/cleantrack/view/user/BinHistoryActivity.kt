package com.example.cleantrack.view.user

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.cleantrack.model.RouteInsightModel
import com.example.cleantrack.repository.RouteInsightRepoImpl
import com.example.cleantrack.ui.theme.Black
import com.example.cleantrack.ui.theme.Blue
import com.example.cleantrack.ui.theme.Green
import com.google.firebase.auth.FirebaseAuth
import java.text.SimpleDateFormat
import java.util.*

class BinHistoryActivity : ComponentActivity() {

    private val repo = RouteInsightRepoImpl()
    private val auth = FirebaseAuth.getInstance()

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            var insightList by remember { mutableStateOf<List<RouteInsightModel>>(emptyList()) }
            var isLoading by remember { mutableStateOf(true) }
            var selectedInsight by remember { mutableStateOf<RouteInsightModel?>(null) }

            // Get the current logged-in user ID
            val currentUserId = auth.currentUser?.uid ?: ""

            LaunchedEffect(Unit) {
                // We call the filtered repository method
                repo.getInsightsByUserId(currentUserId) { success, list ->
                    if (success && list != null) {
                        // Sort by newest first
                        insightList = list.sortedByDescending { it.timestamp }
                    }
                    isLoading = false
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(Blue, Green, Color.White),
                            startY = 0f,
                            endY = 1300f
                        )
                    )
            ) {
                Scaffold(
                    containerColor = Color.Transparent,
                    topBar = {
                        CenterAlignedTopAppBar(
                            title = {
                                Text(
                                    "My Collection History",
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color.White
                                )
                            },
                            navigationIcon = {
                                IconButton(onClick = { finish() }) {
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
                ) { paddingValues ->
                    Box(
                        modifier = Modifier
                            .padding(paddingValues)
                            .fillMaxSize()
                    ) {
                        when {
                            isLoading -> {
                                CircularProgressIndicator(
                                    modifier = Modifier.align(Alignment.Center),
                                    color = Color.White
                                )
                            }
                            insightList.isEmpty() -> {
                                Text(
                                    "No collection history found for you.",
                                    modifier = Modifier.align(Alignment.Center),
                                    color = Color.White.copy(alpha = 0.85f),
                                    fontWeight = FontWeight.Medium
                                )
                            }
                            else -> {
                                LazyColumn(
                                    modifier = Modifier.fillMaxSize(),
                                    contentPadding = PaddingValues(16.dp),
                                    verticalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    items(insightList) { insight ->
                                        RouteInsightCards(insight) {
                                            selectedInsight = insight
                                        }
                                    }
                                }
                            }
                        }

                        // DETAIL DIALOG
                        selectedInsight?.let { insight ->
                            val sdf = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault())
                            val date = sdf.format(Date(insight.timestamp))

                            AlertDialog(
                                onDismissRequest = { selectedInsight = null },
                                shape = RoundedCornerShape(24.dp),
                                containerColor = Color.White,
                                confirmButton = {
                                    TextButton(onClick = { selectedInsight = null }) {
                                        Text("Close", color = Green, fontWeight = FontWeight.Bold)
                                    }
                                },
                                title = {
                                    Column {
                                        Text(date, fontSize = 11.sp, color = Color.DarkGray, fontWeight = FontWeight.Bold)
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Text(insight.routeName, fontSize = 20.sp, fontWeight = FontWeight.ExtraBold, color = Black)
                                    }
                                },
                                text = {
                                    Column {
                                        DetailedRows("AI Feedback", insight.aiResponse)
                                        DetailedRows("Rating", "${insight.rating}/5")
                                        DetailedRows("Status", if (insight.segregated) "Properly Segregated ✅" else "Not Segregated ❌")
                                    }
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
fun RouteInsightCards(insight: RouteInsightModel, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = 1.2.dp,
                color = if (insight.segregated) Green else Color.Red.copy(alpha = 0.6f),
                shape = RoundedCornerShape(18.dp)
            ),
        shape = RoundedCornerShape(18.dp),
        onClick = onClick,
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.95f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = insight.routeName, fontSize = 16.sp, fontWeight = FontWeight.ExtraBold, color = Black)
            Spacer(modifier = Modifier.height(6.dp))
            Text(text = insight.aiResponse, fontSize = 13.sp, color = Color.DarkGray, maxLines = 2)
            Spacer(modifier = Modifier.height(10.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(text = "⭐ ${insight.rating}/5", fontWeight = FontWeight.Bold, color = Green)
                Text(
                    text = if (insight.segregated) "Segregated ✅" else "Unsorted ❌",
                    fontWeight = FontWeight.Bold,
                    color = if (insight.segregated) Green else Color.Red
                )
            }
        }
    }
}

@Composable
fun DetailedRows(label: String, value: String) {
    Column(modifier = Modifier.padding(vertical = 8.dp)) {
        Text(text = label, color = Color.Gray, fontSize = 12.sp, fontWeight = FontWeight.Medium)
        Text(text = value, color = Black, fontSize = 14.sp, fontWeight = FontWeight.Bold)
    }
}