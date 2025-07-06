package edu.bhcc.cho.hocusfocustodo.ui.auth

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.textfield.TextInputEditText
import edu.bhcc.cho.hocusfocustodo.R
import edu.bhcc.cho.hocusfocustodo.data.network.AuthApiService

/**
 * Activity for resetting the user's password using OTP.
 */
class PasswordResetActivity : AppCompatActivity() {

    private lateinit var otpField: TextInputEditText
    private lateinit var newPasswordField: TextInputEditText
    private lateinit var confirmPasswordField: TextInputEditText
    private lateinit var errorText: TextView
    private lateinit var resetButton: Button
    private lateinit var backLink: TextView
    private lateinit var apiService: AuthApiService
    private var email: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_password_reset)

        // Initialize views
        otpField = findViewById(R.id.reset_otp)
        newPasswordField = findViewById(R.id.reset_password)
        confirmPasswordField = findViewById(R.id.confirm_password)
        errorText = findViewById(R.id.reset_error)
        resetButton = findViewById(R.id.reset_button)
        backLink = findViewById(R.id.reset_back)
        apiService = AuthApiService(this)

        Log.d("---RESET_PASSWORD_PAGE_LOADED", "---RESET_PASSWORD_PAGE_LOADED")

        // Get email from intent
        email = intent.getStringExtra("EMAIL")

        // Show demo OTP hint
        Toast.makeText(this, "Check server log for demo OTP.", Toast.LENGTH_LONG).show()

        // Handle back link
        backLink.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }

        // Handle reset button click
        resetButton.setOnClickListener {
            val otp = otpField.text.toString().trim()
            val newPassword = newPasswordField.text.toString().trim()
            val confirmPassword = confirmPasswordField.text.toString().trim()

            // Validate input
            if (otp.isEmpty() || newPassword.isEmpty() || confirmPassword.isEmpty()) {
                errorText.text = "All fields are required."
                errorText.visibility = TextView.VISIBLE
                return@setOnClickListener
            }

            if (newPassword.length < 8) {
                errorText.text = "New password must be at least 8 characters."
                errorText.visibility = TextView.VISIBLE
                return@setOnClickListener
            }

            if (newPassword != confirmPassword) {
                errorText.text = "Passwords must match."
                errorText.visibility = TextView.VISIBLE
                return@setOnClickListener
            }

            // Hide keyboard
            currentFocus?.let { view ->
                val imm = getSystemService(InputMethodManager::class.java)
                imm?.hideSoftInputFromWindow(view.windowToken, 0)
            }

            errorText.visibility = TextView.GONE

            // Call API
            email?.let { safeEmail ->
                apiService.resetPassword(
                    email = safeEmail,
                    newPassword = newPassword,
                    otp = otp,
                    onSuccess = {
                        Toast.makeText(this, "Password changed. Please log in.", Toast.LENGTH_SHORT).show()
                        val intent = Intent(this, LoginActivity::class.java)
                        intent.putExtra("EMAIL", safeEmail)
                        startActivity(intent)
                        finish()
                        Log.d("ResetPassword---", "---Password reset successful, navigating to LoginActivity")
                    },
                    onError = {
                        errorText.text = it
                        errorText.visibility = TextView.VISIBLE
                    }
                )
            } ?: run {
                errorText.text = "Email not found. Please restart the process."
                errorText.visibility = TextView.VISIBLE
            }
        }
    }
}