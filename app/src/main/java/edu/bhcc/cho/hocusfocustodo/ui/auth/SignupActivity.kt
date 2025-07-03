package edu.bhcc.cho.hocusfocustodo.ui.auth

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import edu.bhcc.cho.hocusfocustodo.R
import edu.bhcc.cho.hocusfocustodo.data.model.SignupRequest
import edu.bhcc.cho.hocusfocustodo.data.network.AuthApiService

class SignupActivity : AppCompatActivity() {
//    private lateinit var backRedirect: TextView
    private lateinit var firstNameEditText: EditText
    private lateinit var lastNameEditText: EditText
    private lateinit var emailEditText: EditText
    private lateinit var passwordEditText: EditText
    private lateinit var signupButton: Button
    private lateinit var errorTextView: TextView
    private lateinit var loginRedirect: TextView
    private lateinit var apiService: AuthApiService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signup)

        // Initialize views
//        backRedirect = findViewById<TextView>(R.id.signup_back)
        firstNameEditText = findViewById(R.id.signup_first_name)
        lastNameEditText = findViewById(R.id.signup_last_name)
        emailEditText = findViewById(R.id.signup_email)
        passwordEditText = findViewById(R.id.signup_password)
        signupButton = findViewById(R.id.signup_button)
        errorTextView = findViewById(R.id.signup_error)
        loginRedirect = findViewById(R.id.signup_login_link)

        // Initialize API service
        apiService = AuthApiService(this)

        Log.d("---SIGNUP_PAGE_LOADED", "---SIGNUP_PAGE_LOADED")

//        // "< Back" link to LoginActivity
//       backRedirect.setOnClickListener { finish() } // return to Login

        // Navigate back to LoginActivity
        loginRedirect.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }

        // Handle signup logic
        signupButton.setOnClickListener {
            Log.d("---CONTINUE_BUTTON_CLICKED", "---CONTINUE_BUTTON_CLICKED")
            val firstName = firstNameEditText.text.toString().trim()
            val lastName = lastNameEditText.text.toString().trim()
            val email = emailEditText.text.toString().trim()
            val password = passwordEditText.text.toString()

            ///////////
            // INSERT CONFIRM PASSWORD CODE HERE
            ///////////

            // Basic validation
            if (email.isBlank() || password.isBlank() || firstName.isBlank() || lastName.isBlank()) {
                errorTextView.text = "All fields are required. Password must be 8+ characters."
                errorTextView.visibility = View.VISIBLE
                return@setOnClickListener
            }

            if (password.length < 8) {
                errorTextView.text = "Password must be at least 8 characters."
                errorTextView.visibility = View.VISIBLE
                return@setOnClickListener
            }

            // Call the API
            val request = SignupRequest(email, password, firstName, lastName, extra = null)
            apiService.signupUser(
                request,
                onSuccess = {
                    Log.d("---SIGNUP_SUCCESS", "---SIGNUP_SUCCESS")
                    errorTextView.visibility = View.GONE
                    Toast.makeText(this, "Signup successful. Please log in.", Toast.LENGTH_LONG).show()
                    startActivity(Intent(this, LoginActivity::class.java))
                    finish()
                },
                onError = {
                    Log.d("---SIGNUP_ERROR", "---SIGNUP_ERROR")
                    errorTextView.text = it
                    errorTextView.visibility = View.VISIBLE
                }
            )
        }
    }
}