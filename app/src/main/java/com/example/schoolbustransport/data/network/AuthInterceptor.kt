package com.example.schoolbustransport.data.network

import com.example.schoolbustransport.data.repository.SessionManager
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject

/**
 * AuthInterceptor - OkHttp interceptor for adding authentication tokens to API requests
 * 
 * This interceptor automatically adds the JWT token to all outgoing HTTP requests
 * by reading it from SessionManager and adding it as a Bearer token in the
 * Authorization header.
 * 
 * Implementation details:
 * - Runs on OkHttp's background thread, so blocking calls are acceptable
 * - Uses runBlocking to read the token from Flow (one-time read)
 * - Only adds header if token exists (allows unauthenticated requests if needed)
 * - Token format: "Bearer <token>"
 * 
 * @param sessionManager Provides access to stored authentication token
 */
class AuthInterceptor @Inject constructor(
    private val sessionManager: SessionManager
) : Interceptor {

    /**
     * Intercepts HTTP requests and adds authentication header if token is available
     * 
     * @param chain The interceptor chain containing the request
     * @return The response from the server
     */
    override fun intercept(chain: Interceptor.Chain): Response {
        // Get the original request from the chain
        val originalRequest = chain.request()

        // Retrieve the auth token from DataStore
        // Note: runBlocking is acceptable here because OkHttp interceptors
        // run on a background thread pool, not the main thread
        val authToken = runBlocking { sessionManager.tokenFlow.first() }

        // If a token exists, add it to the request header as Bearer token
        // Otherwise, proceed with the original request (for unauthenticated endpoints)
        val newRequest = if (authToken != null) {
            originalRequest.newBuilder()
                .addHeader("Authorization", "Bearer $authToken")
                .build()
        } else {
            originalRequest
        }

        // Proceed with the modified (or original) request
        return chain.proceed(newRequest)
    }
}
