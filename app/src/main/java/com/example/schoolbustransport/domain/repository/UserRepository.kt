package com.example.schoolbustransport.domain.repository

import com.example.schoolbustransport.domain.model.User
import kotlinx.coroutines.flow.Flow
import java.io.File

/**
 * Repository interface for user-related data.
 */
interface UserRepository {
    fun getUserProfile(userId: String): Flow<User>
    suspend fun updateUserProfile(userId: String, name: String?, phone: String?): Result<User>
    suspend fun deleteAccount(userId: String): Result<Unit>
    suspend fun uploadProfileImage(userId: String, imageFile: File): Result<User>
    suspend fun deleteProfileImage(userId: String): Result<Unit>
}
