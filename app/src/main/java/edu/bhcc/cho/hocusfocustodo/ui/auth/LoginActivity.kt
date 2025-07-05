package edu.bhcc.cho.hocusfocustodo.ui.auth

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import edu.bhcc.cho.hocusfocustodo.R
import edu.bhcc.cho.hocusfocustodo.LoginRequest
import edu.bhcc.cho.hocusfocustodo.data.network.AuthApiService
import edu.bhcc.cho.hocusfocustodo.ui.document.DocumentManagementActivity
import edu.bhcc.cho.hocusfocustodo.utils.JwtUtils
import edu.bhcc.cho.hocusfocustodo.utils.SessionManager
import java.time.Instant

/**
 * LoginActivity handles user authentication by verifying email and password,
 * storing a valid JWT, and navigating to the task overview screen.
 */
class LoginActivity : AppCompatActivity() {

    private lateinit var emailEditText: EditText
    private lateinit var passwordEditText: EditText
    private lateinit var loginButton: Button
    private lateinit var signupLink: TextView
    private lateinit var forgotPasswordLink: TextView
    private lateinit var errorTextView: TextView

    private lateinit var apiService: AuthApiService
    private lateinit var sessionManager: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        // Initialize views
        emailEditText = findViewById(R.id.login_email)
        passwordEditText = findViewById(R.id.login_password)
        loginButton = findViewById(R.id.login_button)
        signupLink = findViewById(R.id.login_signup_link)
        forgotPasswordLink = findViewById(R.id.login_forgot_password_link)
        errorTextView = findViewById(R.id.login_error)

        apiService = AuthApiService(this)
        sessionManager = SessionManager(this)

        Log.d("---LOGIN_PAGE_LOADED", "---LOGIN_PAGE_LOADED")

        // Handle login button click
        loginButton.setOnClickListener {
            val email = emailEditText.text.toString().trim()
            val password = passwordEditText.text.toString().trim()

            if (email.isBlank() || password.isBlank()) {
                errorTextView.text = "Please enter both email and password."
                errorTextView.visibility = View.VISIBLE
                return@setOnClickListener
            }

            val loginRequest = LoginRequest(email, password)

            apiService.loginUser(
                request = loginRequest,
                onSuccess = { token ->
                    val currentTime = System.currentTimeMillis() / 1000
                    val tokenExpirationTime = JwtUtils.getExpirationTime(token.toString())
                    val tokenIssuedAtTime = JwtUtils.getIssuedAtTime(token.toString())

                    // Save session (convert exp to millis)
                    sessionManager.saveSession(
                        token,
                        JwtUtils.getUserId(token) ?: "",
                        (tokenExpirationTime ?: 0) * 1000
                    )

                    Log.d("---PRINT_NEW_JWT", "---TOKEN = $token")
                    Log.d("---NEW_JWT_ISSUED", "---TOKEN iat = ${Instant.ofEpochSecond(tokenIssuedAtTime ?: 0)}")
                    Log.d("---NEW_JWT_EXPIRATION", "---TOKEN exp = ${Instant.ofEpochSecond(tokenExpirationTime ?: 0)}")
                    Log.d("---SYSTEM_TIME", "---SYSTEM TIME now = ${Instant.ofEpochSecond(currentTime)}")
                    Log.d("---JWT_USER_ID", "---TOKEN sub (userId) = ${JwtUtils.getUserId(token) ?: "null"}")

                    errorTextView.visibility = View.GONE
                    startActivity(Intent(this, DocumentManagementActivity::class.java))
                    finish()
                },
                onError = {
                    errorTextView.text = it
                    errorTextView.visibility = View.VISIBLE
                }
            )
        }

        // Handle "Forgot Password?" link
        forgotPasswordLink.setOnClickListener {
            val email = emailEditText.text.toString().trim()
            Log.d("---FORGOT_PASSWORD_LINK_CLICKED", "---FORGOT_PASSWORD_LINK_CLICKED")

            val intent = Intent(this, PasswordForgotActivity::class.java)
            intent.putExtra("EMAIL", email)
            startActivity(intent)
            // Do not call finish() here — let the user return to login if desired
        }

        // Handle "Sign up" link
        signupLink.setOnClickListener {
            Log.d("---SIGNUP_LINK_CLICKED", "---SIGNUP_LINK_CLICKED")
            startActivity(Intent(this, SignupActivity::class.java))
        }
    }
}
