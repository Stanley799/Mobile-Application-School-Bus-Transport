package com.example.schoolbustransport.data.repository

import com.example.schoolbustransport.data.network.ApiService
import com.example.schoolbustransport.data.network.dto.AuthRequest
import com.example.schoolbustransport.data.network.dto.toUser
import com.example.schoolbustransport.domain.model.User
import com.example.schoolbustransport.domain.repository.AuthRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

/**
 * AuthRepositoryImpl - Implementation of AuthRepository interface
 * 
 * Handles authentication operations:
 * - Login: Authenticates user and fetches profile
 * - Logout: Clears stored authentication token
 * - Token management: Stores/retrieves JWT tokens via SessionManager
 * 
 * Login Flow:
 * 1. Call login API with credentials
 * 2. Receive JWT token and user ID
 * 3. Store token in SessionManager (DataStore)
 * 4. Fetch user profile using stored token (AuthInterceptor adds it automatically)
 * 5. Return User object or error
 * 
 * @param apiService Retrofit service for API calls
 * @param sessionManager Manages authentication token storage
 */
class AuthRepositoryImpl @Inject constructor(
    private val apiService: ApiService,
    private val sessionManager: SessionManager
) : AuthRepository {

    /**
     * Authenticates user and retrieves their profile
     * 
     * Process:
     * 1. Send login request with email/password
     * 2. On success, extract token and user ID
     * 3. Save token to DataStore via SessionManager
     * 4. Fetch user profile (token is automatically added by AuthInterceptor)
     * 5. Convert DTO to domain model and return
     * 
     * @param email User's email address
     * @param password User's password
     * @return Result containing User on success, or Exception on failure
     */
    override suspend fun login(email: String, password: String): Result<User> {
        return try {
            val request = AuthRequest(email, password)
            val loginResponse = apiService.login(request)

            if (loginResponse.isSuccessful && loginResponse.body() != null) {
                val authResponse = loginResponse.body()!!
                val userId = authResponse.userIdString // Convert to String
                
                // Save the token to DataStore
                // This happens asynchronously, but we wait for it to complete
                // before making the profile request
                sessionManager.saveToken(authResponse.token)
                
                // Fetch user profile using the stored token
                // AuthInterceptor will automatically add the token to this request
                // Note: There's a small timing consideration here - DataStore writes
                // are async, but typically fast enough that the interceptor will
                // pick up the token. If issues occur, consider adding a small delay.
                val profileResponse = apiService.getUserProfile(userId)

                if (profileResponse.isSuccessful && profileResponse.body() != null) {
                    // Convert DTO to domain model
                    Result.success(profileResponse.body()!!.toUser())
                } else {
                    val errorBody = profileResponse.errorBody()?.string() ?: "Failed to fetch profile after login"
                    Result.failure(Exception("API Error after login (${profileResponse.code()}): $errorBody"))
                }
            } else {
                val errorBody = loginResponse.errorBody()?.string() ?: "Invalid credentials"
                Result.failure(Exception("Login failed: $errorBody"))
            }
        } catch (e: Exception) {
            // Handle network errors, parsing errors, etc.
            Result.failure(e)
        }
    }

    /**
     * Logs out the current user by clearing the stored authentication token
     * 
     * After this call, subsequent API requests will not include authentication headers.
     */
    override suspend fun logout() {
        sessionManager.clearToken()
    }

    /**
     * Gets the currently logged in user
     * 
     * Note: Currently not implemented - returns null flow
     * This could be enhanced to observe user state from DataStore
     * 
     * @return Flow emitting the logged in user, or null if not logged in
     */
    override fun getLoggedInUser(): Flow<User?> = flow {
        emit(null) // Not implemented for this flow
    }

    /**
     * Registers a new user
     * 
     * Note: Currently not implemented - registration may be handled
     * through admin panel or separate registration flow
     * 
     * @param email User's email
     * @param password User's password
     * @param name User's name
     * @param role User's role (ADMIN, DRIVER, PARENT)
     * @return Result containing User on success
     */
    override suspend fun register(email: String, password: String, name: String, role: String): Result<User> {
        return Result.failure(NotImplementedError("Registration is not implemented."))
    }
}