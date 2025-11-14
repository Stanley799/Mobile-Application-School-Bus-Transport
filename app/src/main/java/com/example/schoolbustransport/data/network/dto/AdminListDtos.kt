package com.example.schoolbustransport.data.network.dto

import com.google.gson.annotations.SerializedName

// Lightweight DTOs tailored to the admin list endpoints

data class AdminBusItem(
	val id: Int,
	@SerializedName("number_plate") val numberPlate: String,
	@SerializedName("bus_name") val busName: String?,
	val capacity: Int?,
	val status: String?
)

data class AdminRouteItem(
	val id: Int,
	@SerializedName("route_name") val routeName: String,
	@SerializedName("estimated_time") val estimatedTime: String?
)

data class DriverLiteDto(
	val id: Int,
	@SerializedName("driver_fname") val firstName: String?,
	@SerializedName("driver_lname") val lastName: String?,
	val user: DriverUserLite?
)

data class DriverUserLite(
	val id: Int,
	val name: String?,
	val phone: String?
)

// Request body for creating a trip as admin

data class CreateTripRequest(
	@SerializedName("busId") val busId: Int,
	@SerializedName("routeId") val routeId: Int,
	@SerializedName("driverId") val driverId: Int,
	@SerializedName("studentIds") val studentIds: List<Int> = emptyList(),
	@SerializedName("tripDate") val tripDate: String? = null,
	@SerializedName("tripName") val tripName: String? = null,
	@SerializedName("status") val status: String? = null
)
