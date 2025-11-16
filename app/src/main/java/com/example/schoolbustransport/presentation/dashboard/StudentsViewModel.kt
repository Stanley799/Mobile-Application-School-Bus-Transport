package com.example.schoolbustransport.presentation.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.schoolbustransport.domain.model.Student
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

@HiltViewModel
class StudentsViewModel @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth
) : ViewModel() {

    private val _students = MutableStateFlow<List<Student>>(emptyList())
    val students: StateFlow<List<Student>> = _students

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    init {
        loadMyStudents()
    }

    fun loadMyStudents() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                val userId = auth.currentUser?.uid ?: throw IllegalStateException("User not logged in")
                val snapshot = firestore.collection("students")
                    .whereEqualTo("parentId", userId)
                    .get()
                    .await()
                _students.value = snapshot.toObjects(Student::class.java)
            } catch (e: Exception) {
                _error.value = e.message
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun createStudent(name: String, school: String, grade: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                val userId = auth.currentUser?.uid ?: throw IllegalStateException("User not logged in")
                val student = Student(
                    name = name,
                    school = school,
                    grade = grade,
                    parentId = userId
                )
                firestore.collection("students").add(student).await()
                loadMyStudents() // Refresh list
            } catch (e: Exception) {
                _error.value = e.message
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    fun updateStudent(student: Student) {
        viewModelScope.launch {
            try {
                firestore.collection("students").document(student.id).set(student).await()
                loadMyStudents() // Refresh list
            } catch (e: Exception) {
                _error.value = e.message
            }
        }
    }

    fun deleteStudent(studentId: String) {
        viewModelScope.launch {
            try {
                firestore.collection("students").document(studentId).delete().await()
                loadMyStudents() // Refresh list
            } catch (e: Exception) {
                _error.value = e.message
            }
        }
    }
}
