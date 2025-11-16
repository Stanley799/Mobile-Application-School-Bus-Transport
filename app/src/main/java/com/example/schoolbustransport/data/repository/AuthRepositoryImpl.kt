package com.example.schoolbustransport.data.repository

import android.net.Uri
import com.example.schoolbustransport.domain.model.User
import com.example.schoolbustransport.domain.repository.AuthRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.util.UUID
import javax.inject.Inject

class AuthRepositoryImpl @Inject constructor(
    private val firebaseAuth: FirebaseAuth,
    private val firestore: FirebaseFirestore,
    private val storage: FirebaseStorage
) : AuthRepository {

    override fun getFirebaseUserFlow(): Flow<com.google.firebase.auth.FirebaseUser?> = callbackFlow {
        val authStateListener = FirebaseAuth.AuthStateListener { auth ->
            trySend(auth.currentUser)
        }
        firebaseAuth.addAuthStateListener(authStateListener)
        awaitClose { firebaseAuth.removeAuthStateListener(authStateListener) }
    }

    override suspend fun login(email: String, password: String): Result<User> {
        return try {
            val authResult = firebaseAuth.signInWithEmailAndPassword(email, password).await()
            val firebaseUser = authResult.user!!

            val userDoc = firestore.collection("users").document(firebaseUser.uid).get().await()
            val user = userDoc.toObject<User>()!!

            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun loginWithGoogle(idToken: String): Result<User> {
        return try {
            val credential = GoogleAuthProvider.getCredential(idToken, null)
            val authResult = firebaseAuth.signInWithCredential(credential).await()
            val firebaseUser = authResult.user!!

            val userDocRef = firestore.collection("users").document(firebaseUser.uid)
            val userDoc = userDocRef.get().await()

            val user: User
            if (!userDoc.exists()) {
                val newUser = User(
                    id = firebaseUser.uid,
                    name = firebaseUser.displayName ?: "Google User",
                    email = firebaseUser.email ?: "",
                    role = "" // Set role to empty to trigger role selection
                )
                userDocRef.set(newUser).await()
                user = newUser
            } else {
                user = userDoc.toObject<User>()!!
            }

            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun register(name: String, email: String, phone: String, password: String, role: String): Result<User> {
        return try {
            val authResult = firebaseAuth.createUserWithEmailAndPassword(email, password).await()
            val firebaseUser = authResult.user!!

            val user = User(
                id = firebaseUser.uid,
                name = name,
                email = email,
                phone = phone,
                role = role
            )

            firestore.collection("users").document(firebaseUser.uid).set(user).await()

            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateUserRole(role: String): Result<User> {
        return try {
            val firebaseUser = firebaseAuth.currentUser ?: throw IllegalStateException("User not logged in")
            val userDocRef = firestore.collection("users").document(firebaseUser.uid)

            userDocRef.update("role", role).await()

            val updatedUserDoc = userDocRef.get().await()
            val updatedUser = updatedUserDoc.toObject<User>() ?: throw IllegalStateException("Failed to refetch user data")

            Result.success(updatedUser)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateUserProfile(phone: String, imageUri: Uri?): Result<User> {
        return try {
            val firebaseUser = firebaseAuth.currentUser ?: throw IllegalStateException("User not logged in")
            val userDocRef = firestore.collection("users").document(firebaseUser.uid)

            var imageUrl: String? = null
            if (imageUri != null) {
                val storageRef = storage.reference.child("profile_images/${UUID.randomUUID()}")
                storageRef.putFile(imageUri).await()
                imageUrl = storageRef.downloadUrl.await().toString()
            }

            val updates = mutableMapOf<String, Any>()
            updates["phone"] = phone
            if (imageUrl != null) {
                updates["image"] = imageUrl
            }

            userDocRef.update(updates).await()

            val updatedUserDoc = userDocRef.get().await()
            val updatedUser = updatedUserDoc.toObject<User>() ?: throw IllegalStateException("Failed to refetch user data")

            Result.success(updatedUser)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun getLoggedInUser(): Flow<User?> = callbackFlow {
        val authStateListener = FirebaseAuth.AuthStateListener { auth ->
            val firebaseUser = auth.currentUser
            if (firebaseUser != null) {
                firestore.collection("users").document(firebaseUser.uid).get()
                    .addOnSuccessListener { document ->
                        trySend(document.toObject<User>())
                    }
                    .addOnFailureListener { trySend(null) }
            } else {
                trySend(null)
            }
        }
        firebaseAuth.addAuthStateListener(authStateListener)
        awaitClose { firebaseAuth.removeAuthStateListener(authStateListener) }
    }

    override suspend fun logout() {
        firebaseAuth.signOut()
    }
}
