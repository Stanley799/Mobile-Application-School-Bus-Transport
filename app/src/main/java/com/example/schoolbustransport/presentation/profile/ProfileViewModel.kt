package com.example.schoolbustransport.presentation.profile

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.schoolbustransport.domain.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject


@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore,
    private val storage: FirebaseStorage
) : ViewModel() {

    // Delete account from Firebase Auth and Firestore

    private val _user = MutableStateFlow<User?>(null)
    val user: StateFlow<User?> = _user

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    init {
        loadUserProfile()
    }

    private fun loadUserProfile() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val userId = auth.currentUser?.uid
                if (userId != null) {
                    val snapshot = firestore.collection("users").document(userId).get().await()
                    _user.value = snapshot.toObject(User::class.java)
                }
            } catch (e: Exception) {
                _error.value = e.message
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun updateProfile(userId: String, name: String, phone: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val updates = mapOf("name" to name, "phone" to phone)
                firestore.collection("users").document(userId).update(updates).await()
                loadUserProfile() // Refresh user data
            } catch (e: Exception) {
                _error.value = e.message
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun uploadProfileImage(userId: String, uri: Uri) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                val storageRef = storage.reference.child("profile-images/$userId.jpg")
                val uploadTask = storageRef.putFile(uri)
                uploadTask.await()
                val imageUrl = storageRef.downloadUrl.await()
                firestore.collection("users").document(userId).update("image", imageUrl.toString()).await()
                loadUserProfile() // Refresh user data
            } catch (e: Exception) {
                _error.value = "Failed to upload image: ${e.message ?: "Unknown error"}"
                android.util.Log.e("ProfileViewModel", "Image upload error", e)
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun deleteProfileImage(userId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                firestore.collection("users").document(userId).update("image", null).await()
                loadUserProfile() // Refresh user data
            } catch (e: Exception) {
                _error.value = e.message
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun deleteAccount(userId: String, onComplete: () -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                firestore.collection("users").document(userId).delete().await()
                auth.currentUser?.delete()?.await()
                onComplete()
            } catch (e: Exception) {
                _error.value = e.message
            } finally {
                _isLoading.value = false
            }
        }
    }
}
