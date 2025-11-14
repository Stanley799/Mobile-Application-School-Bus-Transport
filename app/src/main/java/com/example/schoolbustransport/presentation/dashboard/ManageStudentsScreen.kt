package com.example.schoolbustransport.presentation.dashboard

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel

@Composable
fun ManageStudentsScreen(viewModel: StudentsViewModel = hiltViewModel()) {
    // Local form state for creating a new student
    var firstName by remember { mutableStateOf("") }
    var lastName by remember { mutableStateOf("") }
    var admission by remember { mutableStateOf("") }
    var grade by remember { mutableStateOf("") }
    var stream by remember { mutableStateOf("") }

    // Fetch current list on first composition
    LaunchedEffect(Unit) { viewModel.loadMyStudents() }

    val students by viewModel.students.collectAsState()
    val loading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()

    Surface(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Text("My Children", style = MaterialTheme.typography.headlineSmall)
            Spacer(Modifier.height(8.dp))

            if (loading) {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            }
            error?.let {
                if (it.isNotBlank()) {
                    Text(it, color = MaterialTheme.colorScheme.error)
                }
            }

            // List of existing students
            students.forEach { s ->
                ElevatedCard(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                    Column(Modifier.fillMaxWidth().padding(12.dp)) {
                        Text("${s.firstName} ${s.lastName}", style = MaterialTheme.typography.titleMedium)
                        Text("Admission: ${s.admission}", style = MaterialTheme.typography.bodySmall)
                        Text("Grade: ${s.grade ?: "-"}  •  Stream: ${s.stream ?: "-"}", style = MaterialTheme.typography.bodySmall)
                    }
                }
            }

            Spacer(Modifier.height(24.dp))
            HorizontalDivider()
            Spacer(Modifier.height(16.dp))

            Text("Register a Child", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(16.dp))
            OutlinedTextField(
                value = firstName,
                onValueChange = { firstName = it },
                label = { Text("First name") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                value = lastName,
                onValueChange = { lastName = it },
                label = { Text("Last name") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                value = admission,
                onValueChange = { admission = it.filter { ch -> ch.isDigit() } },
                label = { Text("Admission number") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                value = grade,
                onValueChange = { grade = it },
                label = { Text("Grade (optional)") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                value = stream,
                onValueChange = { stream = it },
                label = { Text("Stream (optional)") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(16.dp))
            Button(
                onClick = {
                    val admissionInt = admission.toIntOrNull()
                    if (firstName.isNotBlank() && lastName.isNotBlank() && admissionInt != null) {
                        viewModel.createStudent(firstName, lastName, admissionInt, grade.ifBlank { null }, stream.ifBlank { null })
                        firstName = ""; lastName = ""; admission = ""; grade = ""; stream = ""
                    }
                },
                enabled = !loading && firstName.isNotBlank() && lastName.isNotBlank() && admission.isNotBlank() && admission.toIntOrNull() != null,
                modifier = Modifier.align(Alignment.End)
            ) { Text("Add Child") }
        }
    }
}
