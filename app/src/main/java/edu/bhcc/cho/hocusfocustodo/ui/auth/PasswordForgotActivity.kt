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
 * Activity for handling forgot password requests.
 * Users enter their email to receive a reset code (OTP).
 */
class PasswordForgotActivity : AppCompatActivity() {

    private lateinit var backLink: TextView
    private lateinit var emailInput: TextInputEditText
    private lateinit var errorText: TextView
    private lateinit var continueButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_password_forgot)

        // Initialize views
        backLink = findViewById(R.id.forgot_back)
        emailInput = findViewById(R.id.forgot_email)
        errorText = findViewById(R.id.forgot_error)
        continueButton = findViewById(R.id.forgot_continue_button)

        Log.d("---FORGOT_PASSWORD_PAGE_LOADED", "---FORGOT_PASSWORD_PAGE_LOADED")

        // Navigate back to LoginActivity
        backLink.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }

        // Handle continue button click
        continueButton.setOnClickListener {
            val email = emailInput.text.toString().trim()
            Log.d("ForgotPassword---", "---Email entered: $email")

            if (email.isEmpty()) {
                errorText.text = "Please enter your email address."
                errorText.visibility = TextView.VISIBLE
                return@setOnClickListener
            }

            errorText.visibility = TextView.GONE

            // Hide keyboard
            currentFocus?.let { view ->
                val imm = getSystemService(InputMethodManager::class.java)
                imm?.hideSoftInputFromWindow(view.windowToken, 0)
            }

            // Make API request
            val authApi = AuthApiService(this)
            authApi.requestPasswordReset(
                email = email,
                onSuccess = {
                    Log.d("ForgotPassword---", "---API request successful; check server logs for OTP")
                    Toast.makeText(this, "Check server log for demo OTP.", Toast.LENGTH_LONG).show()

                    // Navigate to PasswordResetActivity
                    val intent = Intent(this, PasswordResetActivity::class.java)
                    intent.putExtra("EMAIL", email)
                    startActivity(intent)
                    finish()
                    Log.d("ForgotPassword---", "---Navigated to PasswordResetActivity")
                },
                onError = {
                    errorText.text = it
                    errorText.visibility = TextView.VISIBLE
                }
            )
        }
    }
}