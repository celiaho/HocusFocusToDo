package edu.bhcc.cho.hocusfocustodo.ui.auth

import android.content.Context
import android.content.Intent
import android.graphics.Rect
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.widget.NestedScrollView
import edu.bhcc.cho.hocusfocustodo.R
import edu.bhcc.cho.hocusfocustodo.data.model.LoginRequest
import edu.bhcc.cho.hocusfocustodo.data.network.AuthApiService
import edu.bhcc.cho.hocusfocustodo.ui.task.TaskOverviewActivity
import edu.bhcc.cho.hocusfocustodo.utils.JwtUtils
import edu.bhcc.cho.hocusfocustodo.utils.SessionManager
import java.time.Instant

/**
 * LoginActivity handles user authentication by verifying email and password,
 * storing a valid JWT, and navigating to the task overview screen.
 */
class LoginActivity : AppCompatActivity() {

    private lateinit var scrollView: NestedScrollView
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

        scrollView = findViewById(R.id.scrollView) // Must match the ID in XML
        scrollView.setOnTouchListener { _, _ -> true } // Disable manual scrolling

        emailEditText = findViewById(R.id.login_email)
        passwordEditText = findViewById(R.id.login_password)
        loginButton = findViewById(R.id.login_button)
        signupLink = findViewById(R.id.login_signup_link)
        forgotPasswordLink = findViewById(R.id.login_forgot_password_link)
        errorTextView = findViewById(R.id.login_error)

        apiService = AuthApiService(this)
        sessionManager = SessionManager(this)

        Log.d("---LOGIN_PAGE_LOADED", "---LOGIN_PAGE_LOADED")

        // Color just "Sign Up" in green
        val fullText = "Don't have an account? Sign Up"
        val signUpText = "Sign Up"
        val spannable = SpannableString(fullText)
        val start = fullText.indexOf(signUpText)
        val end = start + signUpText.length
        spannable.setSpan(
            ForegroundColorSpan(ContextCompat.getColor(this, R.color.auth_green)),
            start,
            end,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        signupLink.text = spannable

        // Login button click
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
                    startActivity(Intent(this, TaskOverviewActivity::class.java))
                    finish()
                },
                onError = {
                    errorTextView.text = it
                    errorTextView.visibility = View.VISIBLE
                }
            )
        }

        // Forgot Password link
        forgotPasswordLink.setOnClickListener {
            val email = emailEditText.text.toString().trim()
            Log.d("---FORGOT_PASSWORD_LINK_CLICKED", "---FORGOT_PASSWORD_LINK_CLICKED")
            val intent = Intent(this, PasswordForgotActivity::class.java)
            intent.putExtra("EMAIL", email)
            startActivity(intent)
        }

        // Sign up link
        signupLink.setOnClickListener {
            Log.d("---SIGNUP_LINK_CLICKED", "---SIGNUP_LINK_CLICKED")
            startActivity(Intent(this, SignupActivity::class.java))
        }

        // 🧠 Keyboard scroll logic
        val rootView = findViewById<View>(android.R.id.content)
        rootView.viewTreeObserver.addOnGlobalLayoutListener {
            val r = Rect()
            rootView.getWindowVisibleDisplayFrame(r)
            val screenHeight = rootView.rootView.height
            val keypadHeight = screenHeight - r.bottom

            if (keypadHeight > screenHeight * 0.15) {
                // keyboard is open → scroll to input
                val focusedView = currentFocus
                focusedView?.let {
                    scrollView.post {
                        scrollView.scrollTo(0, it.bottom)
                    }
                }
            } else {
                // keyboard closed → scroll to top
                scrollView.post {
                    scrollView.scrollTo(0, 0)
                }
            }
        }
    }
}
