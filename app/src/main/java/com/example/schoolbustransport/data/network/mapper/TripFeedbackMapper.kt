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
        id = id,
        tripId = tripId,
        parentId = parentId,
        studentId = studentId,
        rating = rating,
        comment = comment,
        createdAt = date,
        parentName = parentName,
        studentName = studentName
    )
}
