package com.example.schoolbustransport.presentation.trip

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.schoolbustransport.domain.model.TripFeedback

@Composable
fun TripFeedbackListScreen(
    tripId: String,
    viewModel: TripFeedbackViewModel = hiltViewModel()
) {
    val feedbackList by viewModel.feedbackList.collectAsState()
    LaunchedEffect(tripId) { viewModel.loadTripFeedback(tripId) }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("Parent Feedback", style = MaterialTheme.typography.titleLarge)
        Spacer(modifier = Modifier.height(16.dp))
        if (feedbackList.isEmpty()) {
            Text("No feedback submitted yet.", color = Color.Gray)
        } else {
            LazyColumn(modifier = Modifier.fillMaxWidth()) {
                items(feedbackList) { feedback ->
                    FeedbackItem(feedback)
                }
            }
        }
    }
}

@Composable
private fun FeedbackItem(feedback: TripFeedback) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                repeat(feedback.rating) {
                    Icon(Icons.Default.Star, contentDescription = null, tint = Color(0xFFFFC107))
                }
                repeat(5 - feedback.rating) {
                    Icon(Icons.Default.Star, contentDescription = null, tint = Color.LightGray)
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(feedback.parentName ?: "Parent", style = MaterialTheme.typography.bodyMedium)
            }
            if (!feedback.comment.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(feedback.comment, style = MaterialTheme.typography.bodyLarge)
            }
            if (!feedback.studentName.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(2.dp))
                Text("For: ${feedback.studentName}", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
            }
            if (feedback.createdAt != null) {
                Spacer(modifier = Modifier.height(2.dp))
                Text(feedback.createdAt.toString(), style = MaterialTheme.typography.labelSmall, color = Color.Gray)
            }
        }
    }
}
