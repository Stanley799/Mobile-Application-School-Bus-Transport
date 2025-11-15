package com.example.schoolbustransport.data.repository

import com.example.schoolbustransport.data.network.ApiService
import com.example.schoolbustransport.data.network.dto.UpdateUserProfileRequest
import com.example.schoolbustransport.data.network.dto.toUser
import com.example.schoolbustransport.domain.model.User
import com.example.schoolbustransport.domain.repository.UserRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import javax.inject.Inject

class UserRepositoryImpl @Inject constructor(
    private val apiService: ApiService
) : UserRepository {

    override fun getUserProfile(userId: String): Flow<User> = flow {
        try {
            // The AuthInterceptor will automatically add the token to this request.
            val response = apiService.getUserProfile(userId)
            val body = response.body()
            if (response.isSuccessful && body != null) {
                emit(body.toUser())
            } else {
                val errorBody = response.errorBody()?.string() ?: "Failed to fetch user profile"
                throw Exception("API Error (${response.code()}): $errorBody")
            }
        } catch (e: Exception) {
            // Re-throw with context
            throw Exception("Failed to fetch user profile: ${e.message}", e)
        }
    }

    override suspend fun updateUserProfile(userId: String, name: String?, phone: String?): Result<User> {
        return try {
            val request = UpdateUserProfileRequest(name, phone)
            val response = apiService.updateUserProfile(userId, request)
            val body = response.body()
            if (response.isSuccessful && body != null) {
                Result.success(body.toUser())
            } else {
                val errorBody = response.errorBody()?.string() ?: "Failed to update user profile"
                Result.failure(Exception("Update failed: $errorBody"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deleteAccount(userId: String): Result<Unit> {
        return try {
            val response = apiService.deleteAccount(userId)
            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                val errorBody = response.errorBody()?.string() ?: "Failed to delete account"
                Result.failure(Exception("Delete failed: $errorBody"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun uploadProfileImage(userId: String, imageFile: File): Result<User> {
        return try {
            val reqFile = imageFile.asRequestBody("image/*".toMediaTypeOrNull())
            val multipartBody = MultipartBody.Part.createFormData("image", imageFile.name, reqFile)
            val response = apiService.uploadProfileImage(userId, multipartBody)
            val responseBody = response.body()
            if (response.isSuccessful && responseBody != null) {
                Result.success(responseBody.toUser())
            } else {
                val errorBody = response.errorBody()?.string() ?: "Failed to upload image"
                Result.failure(Exception("Upload failed: $errorBody"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deleteProfileImage(userId: String): Result<Unit> {
        return try {
            val response = apiService.deleteProfileImage(userId)
            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                val errorBody = response.errorBody()?.string() ?: "Failed to delete image"
                Result.failure(Exception("Delete failed: $errorBody"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
