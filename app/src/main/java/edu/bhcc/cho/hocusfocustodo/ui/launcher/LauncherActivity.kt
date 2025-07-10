package edu.bhcc.cho.hocusfocustodo.ui.launcher

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import edu.bhcc.cho.hocusfocustodo.R
import edu.bhcc.cho.hocusfocustodo.ui.auth.LoginActivity
import edu.bhcc.cho.hocusfocustodo.ui.task.TaskOverviewActivity
import edu.bhcc.cho.hocusfocustodo.utils.JwtUtils
import edu.bhcc.cho.hocusfocustodo.utils.SessionManager
import java.time.Instant

/**
 * LauncherActivity shows a splash screen and routes to the login or main screen
 * depending on the presence and validity of a JWT token.
 */
class LauncherActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Load splash layout
        setContentView(R.layout.activity_launcher) // your splash screen layout

        Log.d("---SPLASH_PAGE_LOADED", "---SPLASH_PAGE_LOADED")

        val sessionManager = SessionManager(this)
        val token = sessionManager.getToken()
        val currentTime = System.currentTimeMillis() / 1000
        val tokenExpirationTime = JwtUtils.getExpirationTime(token.orEmpty())
        val tokenIssuedAtTime = JwtUtils.getIssuedAtTime(token.orEmpty())

        val nextActivity = if (!token.isNullOrBlank() && !JwtUtils.isTokenExpired(token)) {
            // Log valid token times
            Log.d("---VALID_TOKEN_ISSUED", "---iat (readable) = ${Instant.ofEpochSecond(tokenIssuedAtTime ?: 0)}")
            Log.d("---VALID_TOKEN_EXPIRATION", "---exp (readable) = ${Instant.ofEpochSecond(tokenExpirationTime ?: 0)}")
            Log.d("---SYSTEM_TIME", "---now (readable) = ${Instant.ofEpochSecond(currentTime)}")

            TaskOverviewActivity::class.java // <-- Change this when ready
        } else {
            // Log invalid token times
            Log.d("---INVALID_TOKEN_ISSUED", "---iat (readable) = ${Instant.ofEpochSecond(tokenIssuedAtTime ?: 0)}")
            Log.d("---INVALID_TOKEN_EXPIRATION", "---exp (readable) = ${Instant.ofEpochSecond(tokenExpirationTime ?: 0)}")
            Log.d("---SYSTEM_TIME", "---now (readable) = ${Instant.ofEpochSecond(currentTime)}")

            LoginActivity::class.java
        }

        // Delay routing to allow splash screen to appear (~1.5s)
        Handler(Looper.getMainLooper()).postDelayed({
            startActivity(Intent(this, nextActivity))
            finish()
        }, 1500)
    }
}