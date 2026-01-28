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
import java.text.SimpleDateFormat
import java.util.*

class UserBinHistoryActivity : ComponentActivity() {

    private val repo = RouteInsightRepoImpl()

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {

            var insightList by remember { mutableStateOf<List<RouteInsightModel>>(emptyList()) }
            var isLoading by remember { mutableStateOf(true) }
            var selectedInsight by remember { mutableStateOf<RouteInsightModel?>(null) }

            LaunchedEffect(Unit) {
                repo.getAllInsights { success, list ->
                    if (success && list != null) {
                        insightList = list
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
                                    "Bin Collection History",
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
                                    "No bin collection history found.",
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
                                        RouteInsightCard(insight) {
                                            selectedInsight = insight
                                        }
                                    }
                                }
                            }
                        }

                        // 🔍 DETAIL DIALOG
                        selectedInsight?.let { insight ->
                            val sdf = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault())
                            val date = sdf.format(Date(insight.timestamp))

                            AlertDialog(
                                onDismissRequest = { selectedInsight = null },
                                shape = RoundedCornerShape(24.dp),
                                containerColor = Color.White,
                                confirmButton = {
                                    TextButton(onClick = { selectedInsight = null }) {
                                        Text(
                                            "Close",
                                            color = Green,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                },
                                title = {
                                    Column {
                                        Text(
                                            text = date,
                                            fontSize = 11.sp,
                                            color = Color.DarkGray,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Text(
                                            text = insight.routeName,
                                            fontSize = 20.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = Black
                                        )
                                    }
                                },
                                text = {
                                    Column {
                                        DetailedRow("AI Response", insight.aiResponse)
                                        DetailedRow("Rating", "${insight.rating}/5")
                                        DetailedRow(
                                            "Segregation",
                                            if (insight.segregated)
                                                "Properly Segregated"
                                            else
                                                "Not Segregated"
                                        )
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

/* ----------------------------- CARD UI ----------------------------- */

@Composable
fun RouteInsightCard(
    insight: RouteInsightModel,
    onClick: () -> Unit
) {
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
        colors = CardDefaults.cardColors(
            containerColor = Color.White.copy(alpha = 0.95f)
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {

            Text(
                text = insight.routeName,
                fontSize = 16.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Black
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = insight.aiResponse,
                fontSize = 13.sp,
                color = Color.DarkGray,
                maxLines = 2
            )

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {

                Text(
                    text = "⭐ ${insight.rating}/5",
                    fontWeight = FontWeight.Bold,
                    color = Green
                )

                Text(
                    text = if (insight.segregated)
                        "Segregated ✅"
                    else
                        "Not Segregated ❌",
                    fontWeight = FontWeight.Bold,
                    color = if (insight.segregated) Green else Color.Red
                )
            }
        }
    }
}


/* -------------------------- DETAIL ROW -------------------------- */

@Composable
fun DetailedRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            color = Color.Gray,
            fontWeight = FontWeight.Medium,
            fontSize = 14.sp
        )
        Text(
            text = value,
            color = Black,
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp
        )
    }
}
