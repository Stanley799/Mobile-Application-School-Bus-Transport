package com.example.schoolbustransport.data.repository

import android.content.Context
import android.util.Base64
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.schoolbustransport.domain.model.UserRole
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.nio.charset.Charset
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Extension property for accessing DataStore instance
 * 
 * Creates a DataStore named "session" for storing session-related preferences.
 * This is a top-level property to ensure a single DataStore instance per Context.
 */
private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "session")

/**
 * SessionManager - Manages authentication token storage using DataStore
 * 
 * Responsibilities:
 * - Store JWT authentication tokens securely
 * - Provide reactive Flow for token observation
 * - Clear tokens on logout
 * 
 * Implementation:
 * - Uses DataStore (Preferences) for persistent storage
 * - Provides Flow<String?> for reactive token access
 * - Singleton scope ensures single instance across app
 * 
 * Security Note:
 * - DataStore encrypts data at rest on Android 6.0+
 * - Tokens are stored in app-private storage
 * - Consider using EncryptedSharedPreferences for additional security if needed
 * 
 * @param context Application context for DataStore access
 */
@Singleton
class SessionManager @Inject constructor(@ApplicationContext private val context: Context) {

    companion object {
        /**
         * Key for storing authentication token in DataStore preferences
         */
        private val TOKEN_KEY = stringPreferencesKey("auth_token")
    }

    /**
     * Flow that emits the current authentication token
     * 
     * Emits:
     * - Current token value when available
     * - null when no token is stored
     * 
     * Automatically updates when token is saved or cleared.
     * Used by AuthInterceptor to add tokens to API requests.
     * 
     * @return Flow emitting the current token or null
     */
    val tokenFlow: Flow<String?> = context.dataStore.data
        .map { preferences ->
            preferences[TOKEN_KEY]
        }

    /**
     * Flow that emits the current user's ID, decoded from the JWT.
     */
    val userIdFlow: Flow<Int?> = tokenFlow.map { token ->
        token?.let { decodeUserIdFromToken(it) }
    }

    val userRoleFlow: Flow<UserRole?> = tokenFlow.map { token ->
        token?.let { decodeUserRoleFromToken(it) }
    }

    /**
     * Saves the authentication token to DataStore
     * 
     * This is a suspend function that performs the write operation.
     * The token will be available via tokenFlow after this completes.
     * 
     * @param token JWT authentication token to store
     */
    suspend fun saveToken(token: String) {
        context.dataStore.edit { preferences ->
            preferences[TOKEN_KEY] = token
        }
    }

    /**
     * Clears all stored session data (including authentication token)
     * 
     * Used during logout to remove stored credentials.
     * After this call, tokenFlow will emit null.
     */
    suspend fun clearToken() {
        context.dataStore.edit { preferences ->
            preferences.clear()
        }
    }

    private fun decodeUserIdFromToken(token: String): Int? {
        return try {
            val split = token.split(".")
            val claims = String(Base64.decode(split[1], Base64.URL_SAFE), Charset.defaultCharset())
            // We are manually parsing the JSON here to avoid pulling in a full JSON library
            // for this single use case. This is a bit brittle but avoids the dependency.
            val subject = claims.substringAfter("\"sub\":").substringBefore(",")
            subject.toIntOrNull()
        } catch (e: Exception) {
            // If the token is malformed or the 'sub' claim is not an Int, return null
            null
        }
    }

    private fun decodeUserRoleFromToken(token: String): UserRole? {
        return try {
            val split = token.split(".")
            val claims = String(Base64.decode(split[1], Base64.URL_SAFE), Charset.defaultCharset())
            val role = claims.substringAfter("\"role\":\"").substringBefore("\"")
            UserRole.fromString(role)
        } catch (e: Exception) {
            null
        }
    }
}
