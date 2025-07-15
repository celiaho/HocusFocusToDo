package edu.bhcc.cho.hocusfocustodo.ui.auth

import android.content.Intent
import android.graphics.Rect
import android.os.Bundle
import android.text.SpannableString
import android.text.Spanned
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.text.style.ForegroundColorSpan
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.widget.NestedScrollView
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import edu.bhcc.cho.hocusfocustodo.R
import edu.bhcc.cho.hocusfocustodo.data.model.SignupRequest
import edu.bhcc.cho.hocusfocustodo.data.network.AuthApiService

class SignupActivity : AppCompatActivity() {

    private lateinit var scrollView: NestedScrollView

    private lateinit var emailLayout: TextInputLayout
    private lateinit var firstNameLayout: TextInputLayout
    private lateinit var lastNameLayout: TextInputLayout
    private lateinit var passwordLayout: TextInputLayout
    private lateinit var confirmPasswordLayout: TextInputLayout

    private lateinit var emailEditText: TextInputEditText
    private lateinit var firstNameEditText: TextInputEditText
    private lateinit var lastNameEditText: TextInputEditText
    private lateinit var passwordEditText: TextInputEditText
    private lateinit var confirmPasswordEditText: TextInputEditText

    private lateinit var signupButton: Button
    private lateinit var loginLink: TextView

    private lateinit var apiService: AuthApiService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signup)

        scrollView = findViewById(R.id.scrollView)
        scrollView.setOnTouchListener { _, _ -> true } // Disable manual scrolling

        // Link TextInputLayouts
        emailLayout = findViewById(R.id.signup_email_container)
        firstNameLayout = findViewById(R.id.signup_first_name_container)
        lastNameLayout = findViewById(R.id.signup_last_name_container)
        passwordLayout = findViewById(R.id.signup_password_container)
        confirmPasswordLayout = findViewById(R.id.signup_confirm_password_container)

        // Link EditTexts
        emailEditText = findViewById(R.id.signup_email)
        firstNameEditText = findViewById(R.id.signup_first_name)
        lastNameEditText = findViewById(R.id.signup_last_name)
        passwordEditText = findViewById(R.id.signup_password)
        confirmPasswordEditText = findViewById(R.id.signup_confirm_password)

        // Link button and login link
        signupButton = findViewById(R.id.signup_button)
        loginLink = findViewById(R.id.signup_login_link)

        apiService = AuthApiService(this)

        setupLoginLink()
        setupKeyboardAutoScroll()

        signupButton.setOnClickListener {
            validateAndSignup()
        }
    }

    private fun setupLoginLink() {
        val text = "Already have an account? Log in"
        val spannable = SpannableString(text)
        val start = text.indexOf("Log in")
        val end = start + "Log in".length

        spannable.setSpan(object : ClickableSpan() {
            override fun onClick(widget: View) {
                startActivity(Intent(this@SignupActivity, LoginActivity::class.java))
                finish()
            }
        }, start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)

        spannable.setSpan(
            ForegroundColorSpan(ContextCompat.getColor(this, R.color.auth_green)),
            start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        )

        loginLink.text = spannable
        loginLink.movementMethod = LinkMovementMethod.getInstance()
        loginLink.highlightColor = android.graphics.Color.TRANSPARENT
    }

    private fun setupKeyboardAutoScroll() {
        val rootView = findViewById<View>(android.R.id.content)
        rootView.viewTreeObserver.addOnGlobalLayoutListener {
            val rect = Rect()
            rootView.getWindowVisibleDisplayFrame(rect)
            val screenHeight = rootView.rootView.height
            val keypadHeight = screenHeight - rect.bottom

            if (keypadHeight > screenHeight * 0.15) {
                // Keyboard is open
                val focused = currentFocus
                focused?.let { view ->
                    val scrollY = view.top - 100
                    scrollView.post {
                        scrollView.smoothScrollTo(0, scrollY.coerceAtLeast(0))
                    }
                }
            } else {
                // Keyboard is closed
                scrollView.post {
                    scrollView.smoothScrollTo(0, 0)
                }
            }
        }
    }

    private fun validateAndSignup() {
        emailLayout.error = null
        firstNameLayout.error = null
        lastNameLayout.error = null
        passwordLayout.error = null
        confirmPasswordLayout.error = null

        val email = emailEditText.text.toString().trim()
        val firstName = firstNameEditText.text.toString().trim()
        val lastName = lastNameEditText.text.toString().trim()
        val password = passwordEditText.text.toString()
        val confirmPassword = confirmPasswordEditText.text.toString()

        var valid = true

        if (email.isBlank()) {
            emailLayout.error = "Email is required"
            valid = false
        }

        if (firstName.isBlank()) {
            firstNameLayout.error = "First name is required"
            valid = false
        }

        if (lastName.isBlank()) {
            lastNameLayout.error = "Last name is required"
            valid = false
        }

        if (password.isBlank()) {
            passwordLayout.error = "Password is required"
            valid = false
        } else if (password.length < 8) {
            passwordLayout.error = "Password must be at least 8 characters"
            valid = false
        }

        if (confirmPassword.isBlank()) {
            confirmPasswordLayout.error = "Please confirm your password"
            valid = false
        } else if (confirmPassword.length < 8) {
            confirmPasswordLayout.error = "Confirm password must be at least 8 characters"
            valid = false
        } else if (password != confirmPassword) {
            confirmPasswordLayout.error = "Passwords must match"
            valid = false
        }

        if (!valid) return

        val request = SignupRequest(email, password, firstName, lastName, extra = null)

        apiService.signupUser(
            request,
            onSuccess = {
                startActivity(Intent(this, LoginActivity::class.java))
                finish()
            },
            onError = {
                emailLayout.error = it
            }
        )
    }
}
