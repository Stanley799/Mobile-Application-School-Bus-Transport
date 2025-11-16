package com.example.schoolbustransport.data.network.mapper

import com.example.schoolbustransport.data.network.dto.TripFeedbackDto
import com.example.schoolbustransport.domain.model.TripFeedback
import java.text.SimpleDateFormat
import java.util.*

fun TripFeedbackDto.toDomain(): TripFeedback {
    val parentName = listOfNotNull(parent?.firstName, parent?.lastName).joinToString(" ").takeIf { it.isNotBlank() }
    val studentName = listOfNotNull(student?.firstName, student?.lastName).joinToString(" ").takeIf { it.isNotBlank() }
    val date = createdAt?.let {
        try {
            SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault()).parse(it)
        } catch (e: Exception) { null }
    }
    return TripFeedback(
        id = id.toIntOrNull() ?: -1,
        tripId = tripId?.toIntOrNull() ?: -1,
        parentId = parentId?.toIntOrNull() ?: -1,
        studentId = studentId?.toIntOrNull(),
        rating = rating?.toInt() ?: 0,
        comment = comment,
        createdAt = date,
        parentName = parentName,
        studentName = studentName
    )
}
