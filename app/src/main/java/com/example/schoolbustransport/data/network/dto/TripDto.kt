package com.example.schoolbustransport.data.network.dto

import com.example.schoolbustransport.domain.model.Bus
import com.example.schoolbustransport.domain.model.Route
import com.example.schoolbustransport.domain.model.Trip
import com.example.schoolbustransport.domain.model.TripStatus
import com.example.schoolbustransport.domain.model.User
import com.example.schoolbustransport.domain.model.Student
import com.google.gson.annotations.SerializedName
import java.text.SimpleDateFormat
import java.time.Instant
import java.util.Date
import java.util.Locale

/**
 * DTO for trip data.
 */
data class TripDto(
    val id: Int,
    @SerializedName("trip_date") val tripDate: String?,
    val start: String?,
    val stop: String?,
    val status: String?,
    val bus: TripBusDto?,
    val route: TripRouteDto?,
    val driver: TripDriverDto?,
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
    @SerializedName("driver_fname") val driverFName: String?,
    @SerializedName("driver_lname") val driverLName: String?,
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
    @SerializedName("student_lname") val lastName: String?
)

/**
 * Mapper function to convert TripDto to Trip domain model.
 */
fun TripDto.toTrip(): Trip {
    val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US)
    fun parseIsoFlexible(value: String): Date? {
        return runCatching { Date.from(Instant.parse(value)) }.getOrElse {
            runCatching { sdf.parse(value) }.getOrNull()
        }
    }
    val startDate: Date = when {
        !start.isNullOrBlank() -> parseIsoFlexible(start) ?: Date()
        !tripDate.isNullOrBlank() -> parseIsoFlexible(tripDate) ?: Date()
        else -> Date()
    }
    val endDate: Date? = when {
        !stop.isNullOrBlank() -> parseIsoFlexible(stop)
        else -> null
    }

    val busDomain = Bus(
        id = (bus?.id ?: 0).toString(),
        licensePlate = bus?.numberPlate ?: "",
        model = null,
        capacity = bus?.capacity ?: 0
    )

    val routeDomain = Route(
        id = (route?.id ?: 0).toString(),
        name = route?.routeName ?: "",
        description = null,
        waypoints = emptyList()
    )

    val driverName = driver?.user?.name ?: listOfNotNull(driver?.driverFName, driver?.driverLName).joinToString(" ").trim()
    val driverDomain = User(
        id = (driver?.id ?: 0).toString(),
        email = "",
        name = driverName,
        phone = driver?.user?.phone,
        role = com.example.schoolbustransport.domain.model.UserRole.Driver
    )

    val studentsDomain: List<Student> = (tripAttendanceList ?: emptyList()).mapNotNull { link ->
        val st = link.student
        st?.let {
            Student(
                id = (it.id ?: 0).toString(),
                name = listOfNotNull(it.firstName, it.lastName).joinToString(" ").trim(),
                grade = 0,
                school = "",
                parentId = ""
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
        students = studentsDomain
    )
}
