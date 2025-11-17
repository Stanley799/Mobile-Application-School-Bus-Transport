package com.example.schoolbustransport.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await
import java.io.File
import javax.inject.Inject

data class TripReport(
    val id: String,
    val tripId: String,
    val pdfUrl: String,
    val downloadUrl: String,
    val createdAt: com.google.firebase.Timestamp?
)

class TripReportRepository @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val storage: FirebaseStorage,
    private val auth: FirebaseAuth
) {
    
    suspend fun getTripReport(tripId: String): TripReport? {
        return try {
            val reports = firestore.collection("tripReports")
                .whereEqualTo("tripId", tripId)
                .limit(1)
                .get()
                .await()
            
            if (reports.isEmpty) {
                null
            } else {
                val doc = reports.documents[0]
                TripReport(
                    id = doc.id,
                    tripId = doc.getString("tripId") ?: "",
                    pdfUrl = doc.getString("pdfUrl") ?: "",
                    downloadUrl = doc.getString("downloadUrl") ?: "",
                    createdAt = doc.getTimestamp("createdAt")
                )
            }
        } catch (e: Exception) {
            null
        }
    }
    
    suspend fun getAllTripReports(): List<TripReport> {
        return try {
            val userId = auth.currentUser?.uid ?: return emptyList()
            
            // Get user role
            val userDoc = firestore.collection("users").document(userId).get().await()
            val userRole = userDoc.data?.get("role") as? String ?: "PARENT"
            
            val reportsQuery = when (userRole) {
                "ADMIN" -> {
                    // Admins see all reports
                    firestore.collection("tripReports")
                        .orderBy("createdAt", com.google.firebase.firestore.Query.Direction.DESCENDING)
                }
                "DRIVER" -> {
                    // Drivers see reports for their trips
                    val tripsSnapshot = firestore.collection("trips")
                        .whereEqualTo("driverId", userId)
                        .get()
                        .await()
                    val tripIds = tripsSnapshot.documents.map { it.id }
                    
                    if (tripIds.isEmpty()) {
                        return emptyList()
                    }
                    
                    // Firestore 'in' query limited to 10, so batch if needed
                    if (tripIds.size <= 10) {
                        firestore.collection("tripReports")
                            .whereIn("tripId", tripIds)
                            .orderBy("createdAt", com.google.firebase.firestore.Query.Direction.DESCENDING)
                    } else {
                        // Get all and filter client-side
                        firestore.collection("tripReports")
                            .orderBy("createdAt", com.google.firebase.firestore.Query.Direction.DESCENDING)
                    }
                }
                else -> {
                    // Parents see reports for their children's trips
                    val studentsSnapshot = firestore.collection("students")
                        .whereEqualTo("parentId", userId)
                        .get()
                        .await()
                    val studentIds = studentsSnapshot.documents.map { it.id }.toSet()
                    
                    if (studentIds.isEmpty()) {
                        return emptyList()
                    }
                    
                    // Get trips containing these students
                    val tripsSnapshot = firestore.collection("trips")
                        .whereArrayContainsAny("studentIds", studentIds.toList().take(10))
                        .get()
                        .await()
                    val tripIds = tripsSnapshot.documents.map { it.id }
                    
                    if (tripIds.isEmpty()) {
                        return emptyList()
                    }
                    
                    firestore.collection("tripReports")
                        .whereIn("tripId", tripIds.take(10))
                        .orderBy("createdAt", com.google.firebase.firestore.Query.Direction.DESCENDING)
                }
            }
            
            val reports = reportsQuery.get().await()
            reports.documents.map { doc ->
                TripReport(
                    id = doc.id,
                    tripId = doc.getString("tripId") ?: "",
                    pdfUrl = doc.getString("pdfUrl") ?: "",
                    downloadUrl = doc.getString("downloadUrl") ?: "",
                    createdAt = doc.getTimestamp("createdAt")
                )
            }
        } catch (e: Exception) {
            emptyList()
        }
    }
    
    suspend fun downloadReport(report: TripReport, destinationFile: File): Result<File> {
        return try {
            val storageRef = storage.getReferenceFromUrl(report.downloadUrl)
            storageRef.getFile(destinationFile).await()
            Result.success(destinationFile)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

