package edu.bhcc.cho.hocusfocustodo.utils

import android.content.Context
import android.content.SharedPreferences
import android.system.Os.remove
import androidx.core.content.edit

/**
 * Manages session data e.g. JWT auth token, user ID, and token expiration timestamp.
 * Uses SharedPreferences to persist data across app launches.
 */
class SessionManager(private val context: Context) {

    // SharedPreferences file name (private to the app)
    private val prefsFileName = "GottNotesSession"

    // Reference to the SharedPreferences object
    private val prefs: SharedPreferences =
        context.getSharedPreferences(prefsFileName, Context.MODE_PRIVATE)

    // Keys used to store and retrieve values from SharedPreferences
    private val keyAuthToken = "auth_token" // JWT auth token
    private val keyUserId = "user_id" // Logged-in user’s ID
    private val keyTokenExpiration = "token_expiration" // JWT expiration in epoch millis

    /**
     * Saves the auth token, user ID, and expiration time to SharedPreferences.
     *
     * @param token The JWT token string.
     * @param userId The authenticated user's ID.
     * @param expirationMillis The expiration time of the token in milliseconds since epoch.
     */
    fun saveSession(token: String, userId: String, expirationMillis: Long) {
        prefs.edit().apply {
            putString(keyAuthToken, token)
            putString(keyUserId, userId)
            putLong(keyTokenExpiration, expirationMillis)
            apply() // Commit changes asynchronously
        }
    }

    /**
     * Retrieves the saved JWT auth token.
     *
     * @return The stored token, or null if not set.
     */
    fun getToken(): String? = prefs.getString(keyAuthToken, null)

    /**
     * Retrieves the stored user ID.
     *
     * @return The stored user ID, or null if not set.
     */
    fun getUserId(): String? = prefs.getString(keyUserId, null)

    /**
     * Retrieves the stored token expiration timestamp.
     *
     * @return Expiration time in millis since epoch, or 0 if not set.
     */
    fun getTokenExpiration(): Long = prefs.getLong(keyTokenExpiration, 0L)

    /**
     * Checks if the stored JWT token has expired.
     *
     * @return True if expired or no expiration is saved, false if still valid.
     */
    fun isTokenExpired(): Boolean {
        val expiration = getTokenExpiration()
        return expiration == 0L || System.currentTimeMillis() >= expiration
    }

    /**
     * Clears all stored session values.
     */
    fun clearSession() {
        prefs.edit {
            remove(keyAuthToken)
            remove(keyUserId)
            clear()
        }

        // Clear document cache as part of logout
        val docCache = context.getSharedPreferences("DocumentCache", Context.MODE_PRIVATE)
        docCache.edit { clear() } //// added line to ensure cache is cleared on logout from anywhere

    }
}