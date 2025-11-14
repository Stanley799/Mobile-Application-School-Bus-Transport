import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
    override suspend fun uploadProfileImage(userId: String, imageFile: java.io.File): Result<User> {
        return try {
            val reqFile = imageFile.asRequestBody("image/*".toMediaTypeOrNull())
            val body = MultipartBody.Part.createFormData("image", imageFile.name, reqFile)
            val response = apiService.uploadProfileImage(userId, body)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!.toUser())
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
package com.example.schoolbustransport.data.repository

import com.example.schoolbustransport.data.network.ApiService
import com.example.schoolbustransport.data.network.dto.toUser
import com.example.schoolbustransport.domain.model.User
import com.example.schoolbustransport.domain.repository.UserRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class UserRepositoryImpl @Inject constructor(
    private val apiService: ApiService
) : UserRepository {

    override fun getUserProfile(userId: String): Flow<User> = flow {
        try {
            // The AuthInterceptor will automatically add the token to this request.
            val response = apiService.getUserProfile(userId)
            if (response.isSuccessful && response.body() != null) {
                emit(response.body()!!.toUser())
            } else {
                val errorBody = response.errorBody()?.string() ?: "Failed to fetch user profile"
                throw Exception("API Error (${response.code()}): $errorBody")
            }
        } catch (e: Exception) {
            throw e
        }
    }

    override suspend fun updateUserProfile(userId: String, name: String?, phone: String?): Result<User> {
        return try {
            val request = com.example.schoolbustransport.data.network.dto.UpdateUserProfileRequest(name, phone)
            val response = apiService.updateUserProfile(userId, request)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!.toUser())
            } else {
                val errorBody = response.errorBody()?.string() ?: "Failed to update user profile"
                Result.failure(Exception("Update failed: $errorBody"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}