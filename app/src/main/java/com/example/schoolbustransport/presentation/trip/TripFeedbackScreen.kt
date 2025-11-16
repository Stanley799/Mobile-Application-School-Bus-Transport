package com.example.schoolbustransport.presentation.trip

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import kotlinx.coroutines.launch

@Composable
fun TripFeedbackScreen(
    tripId: String,
    studentId: Int? = null,
    onFeedbackSubmitted: () -> Unit = {},
    viewModel: TripFeedbackViewModel = hiltViewModel()
) {
    val submitState by viewModel.submitState.collectAsState()
    var rating by remember { mutableStateOf(0) }
    var comment by remember { mutableStateOf(TextFieldValue()) }
    val scope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Rate Your Trip", style = MaterialTheme.typography.titleLarge)
        Spacer(modifier = Modifier.height(16.dp))
        Row {
            for (i in 1..5) {
                IconButton(onClick = { rating = i }) {
                    Icon(
                        Icons.Default.Star,
                        contentDescription = null,
                        tint = if (i <= rating) Color(0xFFFFC107) else Color.LightGray
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        OutlinedTextField(
            value = comment,
            onValueChange = { comment = it },
            label = { Text("Comment (optional)") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(24.dp))
        Button(
            onClick = {
                scope.launch {
                    viewModel.submitFeedback(tripId, rating, comment.text, studentId?.toString())
                }
            },
            enabled = rating > 0 && submitState !is SubmitFeedbackState.Loading
        ) {
            if (submitState is SubmitFeedbackState.Loading) {
                CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
            } else {
                Text("Submit Feedback")
            }
        }
        if (submitState is SubmitFeedbackState.Success) {
            LaunchedEffect(Unit) { onFeedbackSubmitted() }
            Text("Thank you for your feedback!", color = Color(0xFF388E3C), modifier = Modifier.padding(top = 16.dp))
        }
        if (submitState is SubmitFeedbackState.Error) {
            Text((submitState as SubmitFeedbackState.Error).message, color = Color.Red, modifier = Modifier.padding(top = 16.dp))
        }
    }
}
