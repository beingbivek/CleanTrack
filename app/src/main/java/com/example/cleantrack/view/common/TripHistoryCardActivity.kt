package com.example.cleantrack.view.common

import com.example.cleantrack.model.TripHistoryUiModel
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun TripHistoryCard(item: TripHistoryUiModel, onClick: () -> Unit) {
    val sdf = remember { SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault()) }
    val dateString = sdf.format(Date(item.trip.startTimestamp))

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = item.trip.routeName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = item.trip.status,
                    style = MaterialTheme.typography.labelSmall,
                    color = if (item.trip.status == "COMPLETED") Color(0xFF4CAF50) else Color(0xFFFF9800),
                    modifier = Modifier
                        .background(
                            color = (if (item.trip.status == "COMPLETED") Color(0xFFE8F5E9) else Color(0xFFFFF3E0)),
                            shape = RoundedCornerShape(4.dp)
                        )
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }

            Spacer(modifier = Modifier.height(4.dp))
            Text(text = dateString, style = MaterialTheme.typography.bodySmall, color = Color.Gray)

            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), thickness = 0.5.dp)

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column {
                    Text(text = "Collected", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                    Text(text = "${item.collectedBins} Bins", fontWeight = FontWeight.SemiBold)
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(text = "Remaining", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                    Text(text = "${item.totalBins - item.collectedBins} Bins", fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}