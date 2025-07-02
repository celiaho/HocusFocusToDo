package edu.bhcc.cho.noteserver.ui.settings

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.widget.SwitchCompat
import androidx.core.content.ContextCompat
import androidx.core.content.edit
import edu.bhcc.cho.noteserver.R
import edu.bhcc.cho.noteserver.data.model.Profile
import edu.bhcc.cho.noteserver.data.network.ProfileApiService
import edu.bhcc.cho.noteserver.ui.auth.LoginActivity
import edu.bhcc.cho.noteserver.ui.document.DocumentActivity
import edu.bhcc.cho.noteserver.ui.document.DocumentManagementActivity
import edu.bhcc.cho.noteserver.utils.SessionManager

class SettingsActivity : AppCompatActivity() {
    private lateinit var themeToggle: SwitchCompat
    private lateinit var firstNameInput: EditText
    private lateinit var lastNameInput: EditText
    private lateinit var saveButton: Button
    private lateinit var deleteAccountButton: Button

    private lateinit var sessionManager: SessionManager
    private lateinit var apiService: ProfileApiService

    private var isProfileLoaded = false // Flag to ensure profile is loaded before saving

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        // Highlight Settings icon in toolbar
        findViewById<ImageButton>(R.id.icon_settings)
            .setColorFilter(ContextCompat.getColor(this, R.color.orange))

        // *Handle toolbar clicks*
        // New Document Button
        findViewById<ImageButton>(R.id.icon_document).setOnClickListener {
            Log.d("---NEW_DOCUMENT_BUTTON_CLICKED", "---NEW_DOCUMENT_BUTTON_CLICKED from Settings")
            getSharedPreferences("DocumentCache", MODE_PRIVATE).edit { clear() } // Clear local cache
            // Open new document
            startActivity(Intent(this, DocumentActivity::class.java).apply {
                putExtra("newDoc", true)
            })
            finish()
        }
        // Document Management Icon
        findViewById<ImageButton>(R.id.icon_open_folder).setOnClickListener {
            startActivity(Intent(this, DocumentManagementActivity::class.java))
            finish()
        }
        // Settings Icon
        findViewById<ImageButton>(R.id.icon_settings).setOnClickListener {
            Toast.makeText(this, "You are already in Settings.", Toast.LENGTH_SHORT).show()        }
        // Logout Icon
        findViewById<ImageButton>(R.id.icon_logout).setOnClickListener {
            getSharedPreferences("DocumentCache", MODE_PRIVATE).edit { clear() } // Clear local cache
            SessionManager(this).clearSession() // Clear token + userId
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }

        // Initialize shared preferences and API
        sessionManager = SessionManager(this)
        apiService = ProfileApiService(this)

        // Connect UI elements
        themeToggle = findViewById(R.id.theme_toggle)
        firstNameInput = findViewById(R.id.settings_first_name)
        lastNameInput = findViewById(R.id.settings_last_name)
        saveButton = findViewById(R.id.settings_save_changes)
        deleteAccountButton = findViewById(R.id.settings_delete_account)

        Log.d("---SETTINGS_PAGE_LOADED", "---SETTINGS_PAGE_LOADED")

        // Load current theme toggle state
        val isDark = AppCompatDelegate.getDefaultNightMode() == AppCompatDelegate.MODE_NIGHT_YES
        themeToggle.isChecked = isDark

        // Toggle listener for theme
        themeToggle.setOnCheckedChangeListener { _, isChecked ->
            val mode = if (isChecked) AppCompatDelegate.MODE_NIGHT_YES
            else AppCompatDelegate.MODE_NIGHT_NO
            AppCompatDelegate.setDefaultNightMode(mode)
            recreate() // Restart activity to apply theme
        }

        // Save name changes
        saveButton.setOnClickListener {
            if (!isProfileLoaded) {
                showToast("Profile not loaded. Please wait.")
                return@setOnClickListener
            }

            val firstName = firstNameInput.text.toString().trim()
            val lastName = lastNameInput.text.toString().trim()

            if (firstName.isEmpty() || lastName.isEmpty()) {
                showToast("First and last names cannot be empty.")
                return@setOnClickListener
            }

            val updatedProfile = Profile(
                id = "", // not needed for update
                email = "", // not needed for update
                firstName = firstName,
                lastName = lastName,
                creationDate = "",
                lastModifiedDate = "",
                extra = null
            )

            apiService.updateProfile(
                updatedProfile,
                onSuccess = { showToast("Name updated successfully.") },
                onError = { error -> showToast("Error: ${error.message}") }
            )
        }

        // Delete account logic
        deleteAccountButton.setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle("Delete Your Account?")
                .setMessage("Are you sure you want to delete your account and documents? This action cannot be undone.")
                .setPositiveButton("Delete") { _, _ ->
                    apiService.deleteProfile(
                        onSuccess = {
                            showToast("Account deleted successfully. You’ve been logged out.")

                            // Clear cache and session
                            getSharedPreferences("DocumentCache", MODE_PRIVATE).edit { clear() }
                            sessionManager.clearSession()

                            // Redirect to Login screen and finish Settings screen
                            val intent = Intent(this, LoginActivity::class.java)
                            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                            startActivity(intent)
                        },
                        onError = { err ->
                            showToast("Failed to delete account: $err")
                        }
                    )
                }
                .setNegativeButton("Cancel", null)
                .show()
        }

        // Fill in fields with current data
        loadProfile()
    }

    private fun loadProfile() {
        // Abort process if no user session (e.g., after account deletion)
        if (sessionManager.getToken() == null || sessionManager.getUserId() == null) {
            Log.d("---LOAD_PROFILE_ABORTED", "---Skipped profile load: no session")
            return
        }

        apiService.getProfile(
            onSuccess = { profile ->
                firstNameInput.setText(profile.firstName)
                lastNameInput.setText(profile.lastName)
                isProfileLoaded = true // allow save only after load
            },
            onError = {
                showToast("Failed to load user profile.")
                isProfileLoaded = false
            }
        )
    }

    private fun showToast(msg: String) {
        Toast.makeText(this, msg, Toast.LENGTH_LONG).show()
    }
}