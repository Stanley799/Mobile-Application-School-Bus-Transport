package com.example.schoolbustransport.presentation.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.schoolbustransport.data.network.ApiService
import com.example.schoolbustransport.data.network.dto.CreateStudentRequest
import com.example.schoolbustransport.data.network.dto.StudentDto
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for parent-facing student management.
 * Keeps the list of children (students) and supports creating a new one.
 */
@HiltViewModel
class StudentsViewModel @Inject constructor(
		fun updateStudent(id: Int, firstName: String?, lastName: String?, grade: String?, stream: String?) {
			viewModelScope.launch {
				_isLoading.value = true
				_error.value = null
				try {
					val resp = api.updateStudent(
						id.toString(),
						com.example.schoolbustransport.data.network.dto.UpdateStudentRequest(
							firstName = firstName,
							lastName = lastName,
							grade = grade,
							stream = stream
						)
					)
					if (resp.isSuccessful) {
						loadMyStudents()
					} else {
						_error.value = resp.errorBody()?.string() ?: "Failed to update student"
					}
				} catch (e: Exception) {
					_error.value = e.message
				} finally {
					_isLoading.value = false
				}
			}
		}

		fun deleteStudent(id: Int) {
			viewModelScope.launch {
				_isLoading.value = true
				_error.value = null
				try {
					val resp = api.deleteStudent(id.toString())
					if (resp.isSuccessful) {
						loadMyStudents()
					} else {
						_error.value = resp.errorBody()?.string() ?: "Failed to delete student"
					}
				} catch (e: Exception) {
					_error.value = e.message
				} finally {
					_isLoading.value = false
				}
			}
		}
	private val api: ApiService
) : ViewModel() {

	private val _students = MutableStateFlow<List<StudentDto>>(emptyList())
	val students: StateFlow<List<StudentDto>> = _students

	private val _isLoading = MutableStateFlow(false)
	val isLoading: StateFlow<Boolean> = _isLoading

	private val _error = MutableStateFlow<String?>(null)
	val error: StateFlow<String?> = _error

	fun loadMyStudents() {
		viewModelScope.launch {
			_isLoading.value = true
			_error.value = null
			try {
				val resp = api.getStudents()
				if (resp.isSuccessful && resp.body() != null) {
					_students.value = resp.body()!!
				} else {
					_error.value = resp.errorBody()?.string() ?: "Failed to load students"
				}
			} catch (e: Exception) {
				_error.value = e.message
			} finally {
				_isLoading.value = false
			}
		}
	}

	fun createStudent(firstName: String, lastName: String, admission: Int, grade: String?, stream: String?) {
		viewModelScope.launch {
			_isLoading.value = true
			_error.value = null
			try {
				val resp = api.createStudent(
					CreateStudentRequest(
						firstName = firstName,
						lastName = lastName,
						admission = admission,
						grade = grade,
						stream = stream
					)
				)
				if (resp.isSuccessful) {
					// Re-query list after successful creation to reflect latest state
					loadMyStudents()
				} else {
					_error.value = resp.errorBody()?.string() ?: "Failed to create student"
				}
			} catch (e: Exception) {
				_error.value = e.message
			} finally {
				_isLoading.value = false
			}
		}
	}
}
