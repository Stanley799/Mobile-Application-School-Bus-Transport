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

    override suspend fun updateUserProfile(userId: String, name: String, phone: String): Result<User> {
        // Not implemented in this example
        return Result.failure(UnsupportedOperationException("Updating user profile not implemented"))
    }
}