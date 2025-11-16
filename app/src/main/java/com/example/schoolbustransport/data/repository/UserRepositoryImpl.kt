package com.example.schoolbustransport.data.repository

import java.io.File
import com.example.schoolbustransport.domain.model.User
import com.example.schoolbustransport.domain.repository.UserRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject


class UserRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth,
    private val storage: FirebaseStorage
) : UserRepository {
    override fun getUserProfile(userId: String): Flow<User> = flow {
        val userDoc = firestore.collection("users").document(userId).get().await()
        val user = userDoc.toObject(User::class.java) ?: throw IllegalStateException("User document not found")
        emit(user)
    }

    override suspend fun updateUserProfile(userId: String, name: String?, phone: String?): Result<User> {
        return try {
            val userRef = firestore.collection("users").document(userId)
            val updates = mutableMapOf<String, Any?>()
            name?.let { updates["name"] = it }
            phone?.let { updates["phone"] = it }
            userRef.update(updates).await()
            val updatedUser = userRef.get().await().toObject(User::class.java) ?: throw IllegalStateException("User not found after update")
            Result.success(updatedUser)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deleteAccount(userId: String): Result<Unit> {
        return try {
            firestore.collection("users").document(userId).delete().await()
            // Optionally delete from Auth and Storage if needed
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun uploadProfileImage(userId: String, imageFile: File): Result<User> {
        return try {
            val storageRef = storage.reference.child("profile_images/$userId")
            val uploadTask = storageRef.putFile(android.net.Uri.fromFile(imageFile)).await()
            val downloadUrl = uploadTask.storage.downloadUrl.await().toString()
            firestore.collection("users").document(userId).update("image", downloadUrl).await()
            val updatedUser = firestore.collection("users").document(userId).get().await().toObject(User::class.java) ?: throw IllegalStateException("User not found after image upload")
            Result.success(updatedUser)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deleteProfileImage(userId: String): Result<Unit> {
        return try {
            val storageRef = storage.reference.child("profile_images/$userId")
            storageRef.delete().await()
            firestore.collection("users").document(userId).update("image", null).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
