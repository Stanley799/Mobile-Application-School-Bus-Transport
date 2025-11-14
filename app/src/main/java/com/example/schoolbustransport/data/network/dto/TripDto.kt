package com.example.schoolbustransport.data.network.dto

import com.example.schoolbustransport.domain.model.Bus
import com.example.schoolbustransport.domain.model.Route
import com.example.schoolbustransport.domain.model.Student
import com.example.schoolbustransport.domain.model.Trip
import com.example.schoolbustransport.domain.model.TripStatus
import com.example.schoolbustransport.domain.model.User
import com.google.gson.annotations.SerializedName
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

// DTOs (Data Transfer Objects) - These match the JSON structure from the backend API.

data class TripDto(
    val id: Int,
    @SerializedName("trip_date") val tripDate: String?,
    val start: String?,
    val stop: String?,
    val status: String?,
    val bus: TripBusDto?,
    val route: TripRouteDto?,
    val driver: TripDriverDto?,
    @SerializedName("departure_time") val departureTime: String?,
    @SerializedName("arrival_time") val arrivalTime: String?,
    @SerializedName("trip_attendance_list") val tripAttendanceList: List<TripStudentLinkDto>?
)

data class TripBusDto(
    val id: Int?,
    @SerializedName("number_plate") val numberPlate: String?,
    val capacity: Int?
)

data class TripRouteDto(
    val id: Int?,
    @SerializedName("route_name") val routeName: String?
)

data class TripDriverDto(
    val id: Int?,
    @SerializedName("driver_fname") val driverFirstName: String?,
    @SerializedName("driver_lname") val driverLastName: String?,
    val user: TripDriverUserDto?
)

data class TripDriverUserDto(
    val name: String?,
    val phone: String?
)

data class TripStudentLinkDto(
    val student: TripStudentDto?
)

data class TripStudentDto(
    val id: Int?,
    @SerializedName("student_fname") val firstName: String?,
    @SerializedName("student_lname") val lastName: String?,
    val grade: String?,
    @SerializedName("parent_id") val parentId: Int?,
    @SerializedName("pickup_latitude") val pickupLat: Double? = null,
    @SerializedName("pickup_longitude") val pickupLng: Double? = null
)

/**
 * Mapper function to convert TripDto to the Trip domain model.
 * This is a critical step to ensure data from the API is correctly transformed for use in the UI.
 */
fun TripDto.toTrip(): Trip {

    /**
     * A robust date parser that tries multiple common ISO 8601 formats.
     * This prevents silent failures if the backend date format changes slightly.
     */
    fun parseIsoFlexible(value: String?): Date? {
        if (value.isNullOrBlank()) return null
        val formats = listOf(
            "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", // Format with milliseconds and Z
            "yyyy-MM-dd'T'HH:mm:ss'Z'",       // Format without milliseconds and Z
            "yyyy-MM-dd'T'HH:mm:ss",         // Local time format
            "yyyy-MM-dd"                     // Date only
        )
        for (format in formats) {
            try {
                val sdf = SimpleDateFormat(format, Locale.US)
                sdf.timeZone = TimeZone.getTimeZone("UTC")
                return sdf.parse(value)
            } catch (e: Exception) {
                // Continue to next format if parsing fails
            }
        }
        return null // Return null if no format matches
    }

    val startDate: Date = parseIsoFlexible(start) ?: parseIsoFlexible(tripDate) ?: Date()
    val endDate: Date? = parseIsoFlexible(stop)

    val busDomain = Bus(
        id = (bus?.id ?: 0).toString(),
        licensePlate = bus?.numberPlate ?: "N/A",
        model = null, // Not provided by API
        capacity = bus?.capacity ?: 0
    )

    val routeDomain = Route(
        id = (route?.id ?: 0).toString(),
        name = route?.routeName ?: "Unknown Route",
        description = null, // Not provided by API
        waypoints = emptyList()
    )

    val driverName = driver?.user?.name ?: listOfNotNull(driver?.driverFirstName, driver?.driverLastName).joinToString(" ").trim()
    val driverDomain = User(
        id = (driver?.id ?: 0).toString(),
        email = "", // Not provided by API
        name = if (driverName.isNotBlank()) driverName else "Unknown Driver",
        phone = driver?.user?.phone,
        role = com.example.schoolbustransport.domain.model.UserRole.Driver
    )

    // Correctly map the list of students, including their grade and parentId.
    val studentsDomain: List<Student> = tripAttendanceList.orEmpty().mapNotNull { link ->
        link.student?.let {
            Student(
                id = (it.id ?: 0).toString(),
                name = listOfNotNull(it.firstName, it.lastName).joinToString(" ").trim(),
                grade = it.grade?.toIntOrNull() ?: 0, // Safely handle grade
                school = "", // Not provided by API, defaulting to empty
                parentId = it.parentId?.toString() ?: "", // Map parentId, defaulting to empty
                pickupLat = it.pickupLat,
                pickupLng = it.pickupLng
            )
        }
    }

    return Trip(
        id = id.toString(),
        startTime = startDate,
        endTime = endDate,
        status = when (status?.uppercase()) {
            "IN_PROGRESS" -> TripStatus.IN_PROGRESS
            "COMPLETED" -> TripStatus.COMPLETED
            "CANCELLED" -> TripStatus.CANCELLED
            else -> TripStatus.SCHEDULED
        },
        bus = busDomain,
        route = routeDomain,
        driver = driverDomain,
        students = studentsDomain,
        departureTime = this.departureTime,
        arrivalTime = this.arrivalTime
    )
}
