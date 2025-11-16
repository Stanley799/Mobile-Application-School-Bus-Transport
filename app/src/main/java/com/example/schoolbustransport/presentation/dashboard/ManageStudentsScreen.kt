package com.example.schoolbustransport.presentation.dashboard

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.schoolbustransport.domain.model.Student

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManageStudentsScreen(navController: NavController, viewModel: StudentsViewModel = hiltViewModel()) {

    val students by viewModel.students.collectAsState()
    val loading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()

    // State for the "Add Student" form
    var newStudentName by remember { mutableStateOf("") }
    var newStudentSchool by remember { mutableStateOf("") }
    var newStudentGrade by remember { mutableStateOf("") }

    // State for dialogs
    var studentToEdit by remember { mutableStateOf<Student?>(null) }
    var studentToDelete by remember { mutableStateOf<Student?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Manage My Children") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {

            // Section to Add a New Student
            Text("Register a Child", style = MaterialTheme.typography.titleLarge)
            Spacer(Modifier.height(16.dp))
            OutlinedTextField(
                value = newStudentName,
                onValueChange = { newStudentName = it },
                label = { Text("Full Name") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                value = newStudentSchool,
                onValueChange = { newStudentSchool = it },
                label = { Text("School Name") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                value = newStudentGrade,
                onValueChange = { newStudentGrade = it },
                label = { Text("Grade") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(16.dp))
            Button(
                onClick = {
                    if (newStudentName.isNotBlank() && newStudentSchool.isNotBlank() && newStudentGrade.isNotBlank()) {
                        viewModel.createStudent(newStudentName, newStudentSchool, newStudentGrade)
                        // Clear fields after creation
                        newStudentName = ""
                        newStudentSchool = ""
                        newStudentGrade = ""
                    }
                },
                enabled = !loading && newStudentName.isNotBlank() && newStudentSchool.isNotBlank() && newStudentGrade.isNotBlank(),
                modifier = Modifier.align(Alignment.End)
            ) {
                Text("Add Child")
            }

            Spacer(Modifier.height(24.dp))
            HorizontalDivider()
            Spacer(Modifier.height(16.dp))

            // Section to Display Existing Students
            Text("My Children", style = MaterialTheme.typography.titleLarge)
            Spacer(Modifier.height(16.dp))

            if (loading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
            } else if (error != null) {
                Text(text = error!!, color = MaterialTheme.colorScheme.error, modifier = Modifier.align(Alignment.CenterHorizontally))
            } else if (students.isEmpty()) {
                Text("You have not registered any children yet.", modifier = Modifier.align(Alignment.CenterHorizontally))
            } else {
                // This Column is inside a vertically scrolling parent, so it doesn't need its own scroll behavior.
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    students.forEach { student ->
                        StudentRow(
                            student = student,
                            onEdit = { studentToEdit = student },
                            onDelete = { studentToDelete = student }
                        )
                    }
                }
            }
        }
    }

    // --- Dialogs ---

    studentToEdit?.let {
        EditStudentDialog(student = it, viewModel = viewModel, onDismiss = { studentToEdit = null })
    }

    studentToDelete?.let {
        DeleteStudentDialog(student = it, viewModel = viewModel, onDismiss = { studentToDelete = null })
    }
}

@Composable
private fun StudentRow(student: Student, onEdit: () -> Unit, onDelete: () -> Unit) {
    ElevatedCard(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(Modifier.weight(1f)) {
                Text(student.name, style = MaterialTheme.typography.titleMedium)
                Text("School: ${student.school}", style = MaterialTheme.typography.bodyMedium)
                Text("Grade: ${student.grade}", style = MaterialTheme.typography.bodyMedium)
            }
            Row {
                IconButton(onClick = onEdit) {
                    Icon(Icons.Default.Edit, contentDescription = "Edit Student")
                }
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete Student")
                }
            }
        }
    }
}

@Composable
private fun EditStudentDialog(student: Student, viewModel: StudentsViewModel, onDismiss: () -> Unit) {
    var studentName by remember { mutableStateOf(student.name) }
    var studentSchool by remember { mutableStateOf(student.school) }
    var studentGrade by remember { mutableStateOf(student.grade ?: "") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit Student Details") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = studentName, onValueChange = { studentName = it }, label = { Text("Full Name") })
                OutlinedTextField(value = studentSchool, onValueChange = { studentSchool = it }, label = { Text("School") })
                OutlinedTextField(value = studentGrade, onValueChange = { studentGrade = it }, label = { Text("Grade") })
            }
        },
        confirmButton = {
            Button(onClick = {
                val updatedStudent = student.copy(
                    name = studentName,
                    school = studentSchool,
                    grade = studentGrade
                )
                viewModel.updateStudent(updatedStudent)
                onDismiss()
            }) { Text("Save") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
private fun DeleteStudentDialog(student: Student, viewModel: StudentsViewModel, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Delete Student") },
        text = { Text("Are you sure you want to delete ${student.name}?") },
        confirmButton = {
            Button(
                onClick = {
                    viewModel.deleteStudent(student.id)
                    onDismiss()
                },
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
            ) { Text("Delete") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
