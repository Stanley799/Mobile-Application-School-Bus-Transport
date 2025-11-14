package com.example.schoolbustransport.domain.repository

import com.example.schoolbustransport.domain.model.User
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for authentication.
 * Defines the contract for auth operations that the data layer must implement.
 */
interface AuthRepository {
    // The login function is responsible for the entire flow and returns the final User object.
    suspend fun login(email: String, password: String): Result<User>
    
    suspend fun register(email: String, password: String, name: String, role: String): Result<User>
    fun getLoggedInUser(): Flow<User?>
    suspend fun logout()
}
