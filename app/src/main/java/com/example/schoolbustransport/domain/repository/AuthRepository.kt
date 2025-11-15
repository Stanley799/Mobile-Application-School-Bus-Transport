package com.example.schoolbustransport.domain.repository

import com.example.schoolbustransport.domain.model.User
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for authentication.
 * Defines the contract for auth operations that the data layer must implement.
 */
interface AuthRepository {
    fun getFirebaseUserFlow(): Flow<com.google.firebase.auth.FirebaseUser?>
    suspend fun login(email: String, password: String): Result<User>
    suspend fun register(name: String, email: String, phone: String, password: String, role: String): Result<User>
    fun getLoggedInUser(): Flow<User?>
    suspend fun logout()
}
