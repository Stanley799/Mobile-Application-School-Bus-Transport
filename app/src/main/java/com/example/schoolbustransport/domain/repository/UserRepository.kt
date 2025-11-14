    suspend fun uploadProfileImage(userId: String, imageFile: java.io.File): Result<User>
    suspend fun deleteProfileImage(userId: String): Result<Unit>
package com.example.schoolbustransport.domain.repository

import com.example.schoolbustransport.domain.model.User
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for user-related data.
 */
interface UserRepository {
    fun getUserProfile(userId: String): Flow<User>
    suspend fun updateUserProfile(userId: String, name: String?, phone: String?): Result<User>
}
