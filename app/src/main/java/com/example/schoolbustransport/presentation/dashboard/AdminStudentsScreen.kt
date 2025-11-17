package com.example.schoolbustransport.presentation.dashboard

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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.schoolbustransport.domain.model.Student
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.toObjects
import kotlinx.coroutines.tasks.await

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminStudentsScreen(navController: NavController) {
    val grades = listOf("Grade5", "Grade6", "Grade7", "Grade8", "Grade9")
    var selectedGrade by remember { mutableStateOf<String?>(null) }
    val firestore = remember { FirebaseFirestore.getInstance() }
    val students = remember { mutableStateOf<List<Student>>(emptyList()) }
    val parents = remember { mutableStateOf<Map<String, com.example.schoolbustransport.domain.model.User>>(emptyMap()) }
    val loading = remember { mutableStateOf(false) }
    val error = remember { mutableStateOf<String?>(null) }

    LaunchedEffect(selectedGrade) {
        loading.value = true
        error.value = null
        try {
            val query = if (selectedGrade != null) {
                firestore.collection("students")
                    .whereEqualTo("grade", selectedGrade)
                    .get()
                    .await()
            } else {
                firestore.collection("students").get().await()
            }
            students.value = query.toObjects(Student::class.java)
            
            // Load parent details
            val parentIds = students.value.mapNotNull { it.parentId }.distinct()
            val parentMap = mutableMapOf<String, com.example.schoolbustransport.domain.model.User>()
            parentIds.forEach { parentId ->
                try {
                    val parentDoc = firestore.collection("users").document(parentId).get().await()
                    if (parentDoc.exists()) {
                        parentMap[parentId] = parentDoc.toObject(com.example.schoolbustransport.domain.model.User::class.java) ?: com.example.schoolbustransport.domain.model.User()
                    }
                } catch (e: Exception) {
                    // Parent not found, continue
                }
            }
            parents.value = parentMap
        } catch (e: Exception) {
            error.value = "Failed to load students: ${e.message ?: "Unknown error"}"
        } finally {
            loading.value = false
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Students") },
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
        ) {
            // Grade filter buttons
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = selectedGrade == null,
                    onClick = { selectedGrade = null },
                    label = { Text("All") }
                )
                grades.forEach { grade ->
                    FilterChip(
                        selected = selectedGrade == grade,
                        onClick = { selectedGrade = grade },
                        label = { Text(grade) }
                    )
                }
            }

            if (error.value != null) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
                ) {
                    Text(
                        "Error: ${error.value}",
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }

            if (loading.value && students.value.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else if (students.value.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No students found", style = MaterialTheme.typography.titleMedium)
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(students.value) { student ->
                        StudentCardWithParent(
                            student = student,
                            parent = parents.value[student.parentId]
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun StudentCardWithParent(student: Student, parent: com.example.schoolbustransport.domain.model.User?) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                student.name,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(8.dp))
            if (student.age != null) {
                Text("Age: ${student.age}", style = MaterialTheme.typography.bodyMedium)
            }
            if (student.gender != null) {
                Text("Gender: ${student.gender}", style = MaterialTheme.typography.bodyMedium)
            }
            if (student.grade != null) {
                Text("Grade: ${student.grade}", style = MaterialTheme.typography.bodyMedium)
            }
            Spacer(Modifier.height(8.dp))
            Divider()
            Spacer(Modifier.height(8.dp))
            Text("Parent Details:", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
            if (parent != null) {
                Text("Name: ${parent.name}", style = MaterialTheme.typography.bodyMedium)
                Text("Phone: ${parent.phone ?: "Not provided"}", style = MaterialTheme.typography.bodyMedium)
            } else {
                Text("Parent not found", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error)
            }
        }
    }
}

