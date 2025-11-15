package com.example.schoolbustransport.presentation.dashboard

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
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
import androidx.navigation.NavController
import com.example.schoolbustransport.data.network.dto.StudentDto

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManageStudentsScreen(navController: NavController, viewModel: StudentsViewModel = hiltViewModel()) {
    // Local form state for creating a new student
    var firstName by remember { mutableStateOf("") }
    var lastName by remember { mutableStateOf("") }
    var admission by remember { mutableStateOf("") }
    var grade by remember { mutableStateOf("") }
    var stream by remember { mutableStateOf("") }

    // Fetch current list on first composition
    LaunchedEffect(Unit) { viewModel.loadMyStudents() }

    val students by viewModel.students.collectAsState()
    // Search/filter state
    var searchQuery by remember { mutableStateOf("") }
    var filterGrade by remember { mutableStateOf("") }
    var filterStream by remember { mutableStateOf("") }
    // Filtered list
    val filteredStudents = students.filter { s ->
        val fullName = listOfNotNull(s.firstName, s.lastName).joinToString(" ").trim()
        val matchesQuery = searchQuery.isBlank() ||
            fullName.contains(searchQuery, ignoreCase = true) ||
            s.admission.toString().contains(searchQuery)
        val matchesGrade = filterGrade.isBlank() || (s.grade?.equals(filterGrade, ignoreCase = true) == true)
        val matchesStream = filterStream.isBlank() || (s.stream?.equals(filterStream, ignoreCase = true) == true)
        matchesQuery && matchesGrade && matchesStream
    }
    val loading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()

    var editStudent by remember { mutableStateOf<StudentDto?>(null) }
    var showDeleteDialog by remember { mutableStateOf<StudentDto?>(null) }

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

            // --- Advanced Search/Filter UI ---
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                label = { Text("Search by name or admission") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(8.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = filterGrade,
                    onValueChange = { filterGrade = it },
                    label = { Text("Grade filter") },
                    modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    value = filterStream,
                    onValueChange = { filterStream = it },
                    label = { Text("Stream filter") },
                    modifier = Modifier.weight(1f)
                )
            }
            Spacer(Modifier.height(12.dp))

            // List of filtered students
            filteredStudents.forEach { s ->
                val fullName = listOfNotNull(s.firstName, s.lastName).joinToString(" ").trim().ifBlank { "Unknown student" }
                ElevatedCard(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                    Row(Modifier.fillMaxWidth().padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text(fullName, style = MaterialTheme.typography.titleMedium)
                            Text("Admission: ${s.admission}", style = MaterialTheme.typography.bodySmall)
                            Text("Grade: ${s.grade ?: "-"}  \u2022  Stream: ${s.stream ?: "-"}", style = MaterialTheme.typography.bodySmall)
                        }
                        IconButton(onClick = { editStudent = s }) {
                            Icon(Icons.Default.Edit, contentDescription = "Edit")
                        }
                        IconButton(onClick = { showDeleteDialog = s }) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete")
                        }
                    }
                }
            }

            // Edit dialog
            editStudent?.let { student ->
                var studentFirstName by remember { mutableStateOf(student.firstName ?: "") }
                var studentLastName by remember { mutableStateOf(student.lastName ?: "") }
                var studentGrade by remember { mutableStateOf(student.grade ?: "") }
                var studentStream by remember { mutableStateOf(student.stream ?: "") }
                AlertDialog(
                    onDismissRequest = { editStudent = null },
                    title = { Text("Edit Student") },
                    text = {
                        Column {
                            OutlinedTextField(value = studentFirstName, onValueChange = { studentFirstName = it }, label = { Text("First name") })
                            OutlinedTextField(value = studentLastName, onValueChange = { studentLastName = it }, label = { Text("Last name") })
                            OutlinedTextField(value = studentGrade, onValueChange = { studentGrade = it }, label = { Text("Grade") })
                            OutlinedTextField(value = studentStream, onValueChange = { studentStream = it }, label = { Text("Stream") })
                        }
                    },
                    confirmButton = {
                        Button(onClick = {
                            viewModel.updateStudent(
                                student.id,
                                studentFirstName.ifBlank { null },
                                studentLastName.ifBlank { null },
                                studentGrade,
                                studentStream
                            )
                            editStudent = null
                        }) { Text("Save") }
                    },
                    dismissButton = {
                        TextButton(onClick = { editStudent = null }) { Text("Cancel") }
                    }
                )
            }

            // Delete confirmation dialog
            showDeleteDialog?.let { student ->
                val fullName = listOfNotNull(student.firstName, student.lastName).joinToString(" ").trim().ifBlank { "this student" }
                AlertDialog(
                    onDismissRequest = { showDeleteDialog = null },
                    title = { Text("Delete Student") },
                    text = { Text("Are you sure you want to delete $fullName?") },
                    confirmButton = {
                        Button(onClick = {
                            viewModel.deleteStudent(student.id)
                            showDeleteDialog = null
                        }) { Text("Delete") }
                    },
                    dismissButton = {
                        TextButton(onClick = { showDeleteDialog = null }) { Text("Cancel") }
                    }
                )
            }

            Spacer(Modifier.height(24.dp))
            HorizontalDivider()
            Spacer(Modifier.height(16.dp))

            Text("Register a Child", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(16.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = firstName,
                    onValueChange = { firstName = it },
                    label = { Text("First name") },
                    modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    value = lastName,
                    onValueChange = { lastName = it },
                    label = { Text("Last name") },
                    modifier = Modifier.weight(1f)
                )
            }
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
                    if (firstName.isNotBlank() && admissionInt != null) {
                        viewModel.createStudent(
                            firstName = firstName,
                            lastName = lastName.ifBlank { "" },
                            admission = admissionInt,
                            grade = grade.ifBlank { null },
                            stream = stream.ifBlank { null }
                        )
                        firstName = ""; lastName = ""; admission = ""; grade = ""; stream = ""
                    }
                },
                enabled = !loading && firstName.isNotBlank() && admission.isNotBlank() && admission.toIntOrNull() != null,
                modifier = Modifier.align(Alignment.End)
            ) { Text("Add Child") }
        }
    }
}
