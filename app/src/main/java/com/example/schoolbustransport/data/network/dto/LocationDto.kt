package com.example.schoolbustransport.data.network.dto

import com.google.gson.annotations.SerializedName

data class LocationDto(
    val id: Int,
    @SerializedName("trip_id") val tripId: Int,
    @SerializedName("driver_id") val driverId: Int,
    val latitude: Double,
    val longitude: Double,
    val speed: Float?,
    val heading: Float?,
    @SerializedName("captured_at") val capturedAt: String
)

data class LocationUpdateRequest(
    @SerializedName("tripId") val tripId: Int,
    val latitude: Double,
    val longitude: Double,
    val speed: Float? = null,
    val heading: Float? = null
)

data class LocationResponse(
    val message: String,
    val location: LocationData
)

data class LocationData(
    val id: Int,
    val latitude: Double,
    val longitude: Double,
    val timestamp: String
)

