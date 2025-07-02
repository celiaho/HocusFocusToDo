package edu.bhcc.cho.noteserver.ui.launcher

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.example.android_capstone_to_do_app.R
import edu.bhcc.cho.noteserver.ui.auth.LoginActivity
import edu.bhcc.cho.noteserver.ui.document.DocumentManagementActivity
import com.example.android_capstone_to_do_app.utils.JwtUtils
import com.example.android_capstone_to_do_app.utils.SessionManager
import java.time.Instant

/**
 * LauncherActivity serves as a splash screen that routes to the appropriate activity
 * depending on whether a valid JWT token is found in SharedPreferences.
 */
class LauncherActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_launcher)

        val sessionManager = SessionManager(this)

        Log.d("---SPLASH_PAGE_LOADED", "---SPLASH_PAGE_LOADED")

        val token = sessionManager.getToken()
        val currentTime = System.currentTimeMillis() / 1000
        val tokenExpirationTime = JwtUtils.getExpirationTime(token.toString())
        val tokenIssuedAtTime = JwtUtils.getIssuedAtTime(token.toString())

        // Check token validity and route accordingly
        if (token.isNullOrBlank()) {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }
        val nextActivity = if (!token.isNullOrBlank() && !JwtUtils.isTokenExpired(token)) {
            // Log valid token issue time, token expiration time, and current system time in readable format
            Log.d("---VALID_TOKEN_ISSUED", "---iat (readable) = ${Instant.ofEpochSecond(tokenIssuedAtTime ?: 0)}")
            Log.d("---VALID_TOKEN_EXPIRATION", "---exp (readable) = ${Instant.ofEpochSecond(tokenExpirationTime ?: 0)}")
            Log.d("---SYSTEM_TIME", "---now (readable) = ${Instant.ofEpochSecond(currentTime)}")

            //// Go to DocumentManagementActivity - - TEMP CHANGE
            LoginActivity::class.java
        } else {
            // Log invalid token issue time, token expiration time, and current system time in readable format
            Log.d("---INVALID_TOKEN_ISSUED", "---iat (readable) = ${Instant.ofEpochSecond(tokenIssuedAtTime ?: 0)}")
            Log.d("---INVALID_TOKEN_EXPIRATION", "---exp (readable) = ${Instant.ofEpochSecond(tokenExpirationTime ?: 0)}")
            Log.d("---SYSTEM_TIME", "---now (readable) = ${Instant.ofEpochSecond(currentTime)}")

            // Go to LoginActivity
            LoginActivity::class.java
        }

        // Delay to show splash for ~1 second
        window.decorView.postDelayed({
            startActivity(Intent(this, nextActivity))
            finish()
        }, 1000) // Delay in milliseconds
    }
}