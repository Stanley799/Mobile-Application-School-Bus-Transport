package com.example.schoolbustransport.presentation.dashboard

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddStudentScreen(navController: NavController, viewModel: StudentsViewModel = hiltViewModel()) {
    val loading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    var showSuccessDialog by remember { mutableStateOf(false) }
    var successStudentName by remember { mutableStateOf("") }

    // State for the "Add Student" form
    var studentName by remember { mutableStateOf("") }
    var studentAge by remember { mutableStateOf("") }
    var studentGender by remember { mutableStateOf("") }
    var studentGrade by remember { mutableStateOf("") }
    var homeLocation by remember { mutableStateOf("") }

    val grades = listOf("Grade5", "Grade6", "Grade7", "Grade8", "Grade9")
    val genders = listOf("Male", "Female", "Other")

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Add Student") },
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
            Text("Register a Child", style = MaterialTheme.typography.titleLarge)
            Spacer(Modifier.height(16.dp))

            OutlinedTextField(
                value = studentName,
                onValueChange = { studentName = it },
                label = { Text("Full Name *") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            Spacer(Modifier.height(12.dp))

            OutlinedTextField(
                value = studentAge,
                onValueChange = { if (it.all { char -> char.isDigit() }) studentAge = it },
                label = { Text("Age *") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            Spacer(Modifier.height(12.dp))

            var genderExpanded by remember { mutableStateOf(false) }
            ExposedDropdownMenuBox(
                expanded = genderExpanded,
                onExpandedChange = { genderExpanded = !genderExpanded }
            ) {
                OutlinedTextField(
                    value = studentGender,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Gender *") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = genderExpanded) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor()
                )
                ExposedDropdownMenu(
                    expanded = genderExpanded,
                    onDismissRequest = { genderExpanded = false }
                ) {
                    genders.forEach { gender ->
                        DropdownMenuItem(
                            text = { Text(gender) },
                            onClick = {
                                studentGender = gender
                                genderExpanded = false
                            }
                        )
                    }
                }
            }
            Spacer(Modifier.height(12.dp))

            var gradeExpanded by remember { mutableStateOf(false) }
            ExposedDropdownMenuBox(
                expanded = gradeExpanded,
                onExpandedChange = { gradeExpanded = !gradeExpanded }
            ) {
                OutlinedTextField(
                    value = studentGrade,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Grade *") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = gradeExpanded) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor()
                )
                ExposedDropdownMenu(
                    expanded = gradeExpanded,
                    onDismissRequest = { gradeExpanded = false }
                ) {
                    grades.forEach { grade ->
                        DropdownMenuItem(
                            text = { Text(grade) },
                            onClick = {
                                studentGrade = grade
                                gradeExpanded = false
                            }
                        )
                    }
                }
            }
            Spacer(Modifier.height(12.dp))

            OutlinedTextField(
                value = homeLocation,
                onValueChange = { homeLocation = it },
                label = { Text("Home Location *") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            Spacer(Modifier.height(24.dp))

            if (error != null) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
                ) {
                    Text(
                        text = "Error: $error",
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        modifier = Modifier.padding(16.dp)
                    )
                }
                Spacer(Modifier.height(16.dp))
            }

            Button(
                onClick = {
                    if (studentName.isNotBlank() && studentAge.isNotBlank() && 
                        studentGender.isNotBlank() && studentGrade.isNotBlank() && 
                        homeLocation.isNotBlank()) {
                        viewModel.createStudent(
                            name = studentName,
                            age = studentAge.toIntOrNull() ?: 0,
                            gender = studentGender,
                            grade = studentGrade,
                            homeLocation = homeLocation
                        )
                        // Show success dialog after a delay
                        successStudentName = studentName
                        // Clear fields
                        studentName = ""
                        studentAge = ""
                        studentGender = ""
                        studentGrade = ""
                        homeLocation = ""
                        showSuccessDialog = true
                    }
                },
                enabled = !loading && studentName.isNotBlank() && studentAge.isNotBlank() && 
                    studentGender.isNotBlank() && studentGrade.isNotBlank() && 
                    homeLocation.isNotBlank(),
                modifier = Modifier.fillMaxWidth()
            ) {
                if (loading) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp))
                } else {
                    Text("Add Child")
                }
            }
        }
    }

    // Success Dialog
    if (showSuccessDialog) {
        AlertDialog(
            onDismissRequest = { showSuccessDialog = false },
            title = { Text("Success!") },
            text = { Text("You have successfully added $successStudentName to the system. Thank you!") },
            confirmButton = {
                Button(onClick = { 
                    showSuccessDialog = false
                    // Optionally navigate back or allow adding another
                }) {
                    Text("OK")
                }
            }
        )
    }
}

