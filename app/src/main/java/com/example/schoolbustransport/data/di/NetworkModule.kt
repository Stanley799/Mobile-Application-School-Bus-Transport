package com.example.schoolbustransport.data.di

import com.example.schoolbustransport.BuildConfig
import com.example.schoolbustransport.data.network.ApiService
import com.example.schoolbustransport.data.network.AuthInterceptor
import com.example.schoolbustransport.data.repository.SessionManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

/**
 * NetworkModule - Dagger Hilt module for network-related dependencies
 * 
 * Provides singleton instances of:
 * - AuthInterceptor: Adds JWT tokens to requests
 * - OkHttpClient: HTTP client with interceptors
 * - Retrofit: REST API client
 * - ApiService: Retrofit interface for API endpoints
 * 
 * All dependencies are scoped as Singleton to ensure a single instance
 * throughout the application lifecycle.
 */
@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    /**
     * Provides AuthInterceptor instance
     * 
     * The interceptor automatically adds authentication tokens to all API requests
     * 
     * @param sessionManager Used to retrieve stored authentication token
     * @return Configured AuthInterceptor instance
     */
    @Provides
    @Singleton
    fun provideAuthInterceptor(sessionManager: SessionManager): AuthInterceptor {
        return AuthInterceptor(sessionManager)
    }

    /**
     * Provides OkHttpClient with interceptors
     * 
     * Interceptors added:
     * - AuthInterceptor: Adds JWT token to requests
     * - HttpLoggingInterceptor: Logs request/response bodies (useful for debugging)
     * 
     * @param authInterceptor Interceptor for adding authentication headers
     * @return Configured OkHttpClient instance
     */
    @Provides
    @Singleton
    fun provideHttpClient(authInterceptor: AuthInterceptor): OkHttpClient {
        // Logging interceptor for debugging - logs full request/response bodies
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
        return OkHttpClient.Builder()
            .addInterceptor(authInterceptor) // Add auth token to requests
            .addInterceptor(logging) // Log requests/responses for debugging
            .build()
    }

    /**
     * Provides Retrofit instance for API calls
     * 
     * Configuration:
     * - Base URL from BuildConfig (set in build.gradle.kts)
     * - Gson converter for JSON serialization/deserialization
     * - Custom OkHttpClient with interceptors
     * 
     * @param httpClient Configured OkHttpClient with interceptors
     * @return Configured Retrofit instance
     */
    @Provides
    @Singleton
    fun provideRetrofit(httpClient: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            // Base URL is read from BuildConfig, which is generated from build.gradle.kts
            // Format: "https://your-server.com/api/" (must end with /)
            .baseUrl(BuildConfig.BASE_URL)
            .addConverterFactory(GsonConverterFactory.create()) // JSON converter
            .client(httpClient) // Use our configured HTTP client
            .build()
    }

    /**
     * Provides ApiService interface implementation
     * 
     * This is the main interface used throughout the app for API calls.
     * Retrofit generates the implementation at compile time.
     * 
     * @param retrofit Configured Retrofit instance
     * @return ApiService implementation
     */
    @Provides
    @Singleton
    fun provideApiService(retrofit: Retrofit): ApiService {
        return retrofit.create(ApiService::class.java)
    }
}