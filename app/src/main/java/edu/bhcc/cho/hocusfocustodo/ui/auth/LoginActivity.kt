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
import edu.bhcc.cho.hocusfocustodo.data.model.LoginRequest
import edu.bhcc.cho.hocusfocustodo.data.network.AuthApiService
import edu.bhcc.cho.hocusfocustodo.ui.document.DocumentManagementActivity
import edu.bhcc.cho.hocusfocustodo.utils.JwtUtils
import edu.bhcc.cho.hocusfocustodo.utils.SessionManager
import java.time.Instant

class LoginActivity : AppCompatActivity() {
    private lateinit var emailEditText: EditText
    private lateinit var passwordEditText: EditText
    private lateinit var loginButton: Button
    private lateinit var createAccountButton: Button
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
        createAccountButton = findViewById(R.id.create_account_button)
        forgotPasswordLink = findViewById(R.id.forgot_password_link)
        errorTextView = findViewById(R.id.login_error)

        apiService = AuthApiService(this)
        sessionManager = SessionManager(this)

        Log.d("---LOGIN_PAGE_LOADED", "---LOGIN_PAGE_LOADED")

//        // If valid token exists, skip login and go to DocumentManagementActivity - REPLACED BY SPLASH LOGIC
//        val token = sessionManager.getToken()
//        if (!token.isNullOrBlank() && !JwtUtils.isTokenExpired(token)) {
//            startActivity(Intent(this, DocumentManagementActivity::class.java))
//            finish()
//            return  // Don't run login screen logic
//        }

        // Log token and User ID
        val prefs = getSharedPreferences("GottNotesSession", MODE_PRIVATE)
        for ((key, value) in prefs.all) {
            Log.d("---SHARED_PREFS", "---$key = ${value.toString()}")
        }

//        // Attempt to automatically show the keyboard on email field
//        emailEditText.postDelayed({
//            emailEditText.requestFocus()
//            val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
//            imm.showSoftInput(emailEditText, InputMethodManager.SHOW_IMPLICIT)
////            emailEditText.setOnFocusChangeListener { _, hasFocus ->
////                if (hasFocus) {
////                    val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
////                    imm.showSoftInput(emailEditText, InputMethodManager.SHOW_IMPLICIT)
////                }
////            }
//        }, 100)  // Delay to ensure the layout is fully loaded

        // Handle login button click
        loginButton.setOnClickListener {
            val email = emailEditText.text.toString().trim()
            val password = passwordEditText.text.toString().trim()

            if (email.isBlank() || password.isBlank()) {
                errorTextView.text = "Please enter both email and password."
                errorTextView.visibility = View.VISIBLE
                return@setOnClickListener
            }

            // Send login request to API via AuthApiService
            val loginRequest = LoginRequest(email, password)

            // Call /auth/login and save token, user ID, and expiration time
            apiService.loginUser(
                request = loginRequest,
                onSuccess = { token ->
                    val currentTime = System.currentTimeMillis() / 1000
                    val tokenExpirationTime = JwtUtils.getExpirationTime(token.toString())
                    val tokenIssuedAtTime = JwtUtils.getIssuedAtTime(token.toString())
                    val patchedExpirationMillis = (tokenExpirationTime!! + 3600) * 1000

                    sessionManager.saveSession(
                        token,
                        JwtUtils.getUserId(token) ?: "",
                        patchedExpirationMillis
                    )

                    // Log new token details and current system time in readable format
                    Log.d("---PRINT_NEW_JWT", "---TOKEN = $token")
                    Log.d("---NEW_JWT_ISSUED", "---TOKEN iat = ${Instant.ofEpochSecond(tokenIssuedAtTime ?: 0)}")
                    Log.d("---NEW_JWT_EXPIRATION", "---TOKEN exp = ${Instant.ofEpochSecond(tokenExpirationTime ?: 0)}")
                    Log.d("---SYSTEM_TIME", "---SYSTEM TIME now = ${Instant.ofEpochSecond(currentTime)}")
                    Log.d("---JWT_USER_ID", "---TOKEN sub (userId) = ${JwtUtils.getUserId(token) ?: "null"}")

                    errorTextView.visibility = View.GONE
                    val intent = Intent(this, DocumentManagementActivity::class.java)
                    startActivity(intent)
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

            //// Pass Login email to PasswordForgot screen - DOESN'T WORK
            val intent = Intent(this, PasswordForgotActivity::class.java)
            intent.putExtra("EMAIL", email)
            startActivity(intent)
            finish()
        }

        // Handle "Create Account" button
        createAccountButton.setOnClickListener {
            Log.d("---CREATE_ACCOUNT_BUTTON_CLICKED", "---CREATE_ACCOUNT_BUTTON_CLICKED")
            startActivity(Intent(this, SignupActivity::class.java))
        }
    }
}