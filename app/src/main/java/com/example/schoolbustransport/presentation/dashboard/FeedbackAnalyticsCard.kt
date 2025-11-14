package com.example.schoolbustransport.presentation.dashboard

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.schoolbustransport.domain.model.TripFeedback

@Composable
fun FeedbackAnalyticsCard(
    averageRating: Float,
    feedbackCount: Int,
    recentFeedback: List<TripFeedback>,
    onViewAll: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Star, contentDescription = null, tint = Color(0xFFFFC107), modifier = Modifier.size(28.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Trip Feedback", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.weight(1f))
                Text(String.format("%.1f", averageRating), fontWeight = FontWeight.Bold, fontSize = 20.sp, color = Color(0xFFFFC107))
                Text("/5", color = Color.Gray, fontSize = 16.sp)
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text("$feedbackCount feedback entries", color = Color.Gray, fontSize = 14.sp)
            Spacer(modifier = Modifier.height(8.dp))
            if (recentFeedback.isEmpty()) {
                Text("No recent feedback.", color = Color.Gray)
            } else {
                recentFeedback.take(2).forEach { fb ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        repeat(fb.rating) {
                            Icon(Icons.Default.Star, contentDescription = null, tint = Color(0xFFFFC107), modifier = Modifier.size(16.dp))
                        }
                        repeat(5 - fb.rating) {
                            Icon(Icons.Default.Star, contentDescription = null, tint = Color.LightGray, modifier = Modifier.size(16.dp))
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(fb.comment ?: "(No comment)", fontSize = 14.sp, maxLines = 1)
                    }
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Button(onClick = onViewAll, modifier = Modifier.align(Alignment.End), content = { Text("View All Feedback") })
        }
    }
}
