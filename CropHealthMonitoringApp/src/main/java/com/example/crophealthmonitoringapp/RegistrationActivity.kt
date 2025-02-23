package com.example.crophealthmonitoringapp

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.widget.Button
import android.widget.EditText
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class RegistrationActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_registration)

        // Initialize Firebase Auth
        auth = FirebaseAuth.getInstance()

        // Initialize the views
        val fullNameEditText = findViewById<EditText>(R.id.fullNameEditText)
        val emailEditText = findViewById<EditText>(R.id.emailEditText)
        val passwordEditText = findViewById<EditText>(R.id.passwordEditText)
        val roleRadioGroup = findViewById<RadioGroup>(R.id.roleRadioGroup)
        val registerButton = findViewById<Button>(R.id.registerButton)

        // Set register button click listener
        registerButton.setOnClickListener {
            val fullName = fullNameEditText.text.toString().trim()
            val email = emailEditText.text.toString().trim()
            val password = passwordEditText.text.toString().trim()

            // Get selected role
            val selectedRoleButtonId = roleRadioGroup.checkedRadioButtonId
            val role = if (selectedRoleButtonId != -1) {
                findViewById<RadioButton>(selectedRoleButtonId).text.toString()
            } else {
                ""
            }

            // Validate the input fields
            if (fullName.isEmpty() || email.isEmpty() || password.isEmpty() || role.isEmpty()) {
                Toast.makeText(this, "Please fill out all fields", Toast.LENGTH_SHORT).show()
            } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                Toast.makeText(this, "Please enter a valid email", Toast.LENGTH_SHORT).show()
            } else {
                // Create user with email and password
                auth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            // User registration successful, save extra details to Realtime Database
                            val userId = auth.currentUser?.uid
                            val database = FirebaseDatabase.getInstance().reference
                            val userMap = mapOf(
                                "fullName" to fullName,
                                "email" to email,
                                "role" to role
                            )

                            userId?.let {
                                database.child("users").child(it).setValue(userMap)
                                    .addOnCompleteListener { dbTask ->
                                        if (dbTask.isSuccessful) {
                                            if (role == "Expert") {
                                                // Add detailed expert profile to the experts node
                                                val expertData = mapOf(
                                                    "id" to userId,
                                                    "name" to fullName,
                                                    "specialization" to "Plant Diseases", // Customize or prompt for this
                                                    "role" to "expert",
                                                    "bio" to "Enter your bio here", // Optionally prompt for bio
                                                    "experience" to 0, // Default experience
                                                    "certifications" to mapOf("degree" to "PhD in Plant Pathology") // Example certification
                                                )

                                                database.child("experts").child(userId).setValue(expertData)
                                                    .addOnCompleteListener { expertTask ->
                                                        if (expertTask.isSuccessful) {
                                                            redirectToDashboard("ExpertChatListActivity")
                                                        } else {
                                                            Toast.makeText(
                                                                this,
                                                                "Failed to register expert details: ${expertTask.exception?.message}",
                                                                Toast.LENGTH_LONG
                                                            ).show()
                                                        }
                                                    }
                                            } else {
                                                // Redirect farmers to the MainActivity
                                                redirectToDashboard("MainActivity")
                                            }
                                        } else {
                                            Toast.makeText(
                                                this,
                                                "Failed to save user details: ${dbTask.exception?.message}",
                                                Toast.LENGTH_LONG
                                            ).show()
                                        }
                                    }
                            }
                        } else {
                            // Registration failed
                            Toast.makeText(this, "Registration Failed: ${task.exception?.message}", Toast.LENGTH_LONG).show()
                        }
                    }
            }
        }
    }

    private fun redirectToDashboard(activityName: String) {
        val intent = when (activityName) {
            "ExpertChatListActivity" -> Intent(this, ExpertChatListActivity::class.java)
            else -> Intent(this, MainActivity::class.java)
        }
        startActivity(intent)
        finish() // Close registration activity
    }
}
