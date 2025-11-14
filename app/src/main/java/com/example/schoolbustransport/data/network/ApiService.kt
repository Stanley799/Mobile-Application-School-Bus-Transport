package com.example.schoolbustransport.data.network

import com.example.schoolbustransport.data.network.dto.*
import retrofit2.Response
import retrofit2.http.*

/**
 * ApiService - Retrofit interface defining all REST API endpoints
 * 
 * This interface defines the contract for all HTTP API calls in the application.
 * Retrofit generates the implementation at compile time based on these annotations.
 * 
 * All endpoints are relative to the base URL configured in NetworkModule.
 * Authentication tokens are automatically added by AuthInterceptor.
 * 
 * Endpoint Categories:
 * - Auth: User authentication
 * - Trips: Trip management and operations
 * - User: User profile operations
 * - Location: Real-time location tracking
 * - Messages: In-app messaging
 * - Students: Student management
 * - Admin: Administrative operations (buses, routes, drivers)
 * 
 * Note: Response types use Retrofit's Response wrapper to access
 * status codes and error bodies. All methods are suspend functions
 * for coroutine support.
 */
interface ApiService {

    // --- Trip Feedback --- //

    /**
     * Submit parent feedback for a trip.
     * @param tripId Trip ID
     * @param request TripFeedbackRequest (rating, comment, studentId)
     */
    @POST("trips/{id}/feedback")
    suspend fun submitTripFeedback(
        @Path("id") tripId: String,
        @Body request: TripFeedbackRequest
    ): Response<TripFeedbackDto>

    /**
     * Get all feedback for a trip.
     * @param tripId Trip ID
     */
    @GET("trips/{id}/feedback")
    suspend fun getTripFeedback(
        @Path("id") tripId: String
    ): Response<List<TripFeedbackDto>>

    // --- Messaging: Available Recipients --- //
    @GET("messages/recipients")
    suspend fun getAvailableRecipients(): Response<List<UserLite>>

    // --- Auth --- //

    @POST("auth/login")
    suspend fun login(@Body authRequest: AuthRequest): Response<AuthResponse>

    // --- Trips --- //

    @GET("trips")
    suspend fun getTrips(
        @Query("date") date: String? = null,
        @Query("summary") summary: Boolean = false
    ): Response<List<TripDto>>

    @GET("trips/{id}")
    suspend fun getTripDetails(@Path("id") tripId: String): Response<TripDto>

    @POST("trips")
    suspend fun createTrip(@Body request: CreateTripRequest): Response<TripDto>

    @PUT("trips/{id}/start")
    suspend fun startTrip(@Path("id") tripId: String): Response<TripDto>

    @PUT("trips/{id}/end")
    suspend fun endTrip(@Path("id") tripId: String): Response<TripDto>

    @POST("trips/{id}/attendance")
    suspend fun markAttendance(
        @Path("id") tripId: String,
        @Body request: AttendanceRequest
    ): Response<AttendanceDto>

    @GET("trips/{id}/report")
    suspend fun getTripReport(@Path("id") tripId: String): Response<okhttp3.ResponseBody>

    // --- User --- //

    @GET("users/profile/{id}")
    suspend fun getUserProfile(@Path("id") userId: String): Response<UserDto>

    @GET("users/me")
    suspend fun getMyProfile(): Response<UserDto>

    // --- Location --- //

    @POST("locations")
    suspend fun updateLocation(@Body request: LocationUpdateRequest): Response<LocationResponse>

    @GET("locations/trip/{tripId}")
    suspend fun getTripLocations(@Path("tripId") tripId: String): Response<List<LocationDto>>

    @GET("locations/trip/{tripId}/latest")
    suspend fun getLatestLocation(@Path("tripId") tripId: String): Response<LocationDto>

    // --- Messages --- //

    @POST("messages")
    suspend fun sendMessage(@Body request: SendMessageRequest): Response<MessageDto>

    @GET("messages/conversations")
    suspend fun getConversations(): Response<List<ConversationDto>>

    @GET("messages/{userId}")
    suspend fun getMessages(@Path("userId") userId: String): Response<List<MessageDto>>

    // --- Students --- //

    @POST("students")
    suspend fun createStudent(@Body request: CreateStudentRequest): Response<StudentDto>

    @GET("students")
    suspend fun getStudents(): Response<List<StudentDto>>

    @GET("students/{id}")
    suspend fun getStudentById(@Path("id") studentId: String): Response<StudentDto>

    @PUT("students/{id}")
    suspend fun updateStudent(
        @Path("id") studentId: String,
        @Body request: UpdateStudentRequest
    ): Response<StudentDto>

    // --- Admin (lists for scheduling) --- //

    @GET("admin/buses")
    suspend fun listBuses(): Response<List<AdminBusItem>>

    @GET("admin/routes")
    suspend fun listRoutes(): Response<List<AdminRouteItem>>

    @GET("admin/drivers")
    suspend fun listDrivers(): Response<List<DriverLiteDto>>
}
