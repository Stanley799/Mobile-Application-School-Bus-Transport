
// AllFeedbackScreen: Displays all trip feedback for admin review, with search and filtering.
package com.example.schoolbustransport.presentation.dashboard


// Compose and project imports for UI, state, and feedback model
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.schoolbustransport.domain.model.TripFeedback


// Main composable for viewing all trip feedback
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AllFeedbackScreen(
    viewModel: FeedbackAnalyticsViewModel = hiltViewModel(),
    onBack: () -> Unit = {}
) {
    // Collect feedbacks and search query state
    val feedbacks by viewModel.feedbacks.collectAsState()
    var searchQuery by remember { mutableStateOf(TextFieldValue()) }
    // Filter feedbacks by comment, parent, or student name
    val filtered = feedbacks.filter {
        (it.comment?.contains(searchQuery.text, ignoreCase = true) == true) ||
        (it.parentName?.contains(searchQuery.text, ignoreCase = true) == true) ||
        (it.studentName?.contains(searchQuery.text, ignoreCase = true) == true)
    }
    // Load all feedback on first composition
    LaunchedEffect(Unit) { viewModel.loadAllFeedback() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("All Trip Feedback") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.Search, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize().padding(16.dp)) {
            // Search field for filtering feedback
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                label = { Text("Search feedback, parent, or student") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            Spacer(modifier = Modifier.height(16.dp))
            // Show feedback list or empty state
            if (filtered.isEmpty()) {
                Text("No feedback found.", color = Color.Gray)
            } else {
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    items(filtered) { fb ->
                        FeedbackListItem(fb)
                    }
                }
            }
        }
    }
}


// Card for displaying a single feedback item
@Composable
private fun FeedbackListItem(feedback: TripFeedback) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                repeat(feedback.rating) {
                    Icon(Icons.Default.Star, contentDescription = null, tint = Color(0xFFFFC107), modifier = Modifier.size(16.dp))
                }
                repeat(5 - feedback.rating) {
                    Icon(Icons.Default.Star, contentDescription = null, tint = Color.LightGray, modifier = Modifier.size(16.dp))
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(feedback.parentName ?: "Parent", fontSize = 15.sp, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
                if (!feedback.studentName.isNullOrBlank()) {
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("For: ${feedback.studentName}", fontSize = 13.sp, color = Color.Gray)
                }
            }
            if (!feedback.comment.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(feedback.comment, fontSize = 15.sp)
            }
            if (feedback.createdAt != null) {
                Spacer(modifier = Modifier.height(2.dp))
                Text(feedback.createdAt.toString(), fontSize = 12.sp, color = Color.Gray)
            }
        }
    }
}
