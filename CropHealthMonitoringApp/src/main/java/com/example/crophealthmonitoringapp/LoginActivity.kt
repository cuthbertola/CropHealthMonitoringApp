package com.example.crophealthmonitoringapp

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.crophealthmonitoringapp.databinding.ActivityLoginBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var databaseReference: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize Firebase Auth
        auth = FirebaseAuth.getInstance()

        // Initialize views using ViewBinding
        val emailEditText = binding.usernameEditText
        val passwordEditText = binding.passwordEditText
        val loginButton = binding.loginButton
        val signUpPromptTextView = binding.signupPrompt
        val signUpFarmerButton = binding.registerFarmerButton
        val signUpExpertButton = binding.registerExpertButton

        // Set login button click listener
        loginButton.setOnClickListener {
            val email = emailEditText.text.toString().trim()
            val password = passwordEditText.text.toString().trim()

            if (email.isNotEmpty() && password.isNotEmpty()) {
                // Login with Firebase Authentication
                auth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            // Login successful, fetch user role
                            val userId = auth.currentUser?.uid
                            if (userId != null) {
                                fetchUserRoleAndRedirect(userId)
                            } else {
                                Toast.makeText(this, "Failed to retrieve user details", Toast.LENGTH_SHORT).show()
                            }
                        } else {
                            // Login failed
                            Toast.makeText(this, "Login failed: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                        }
                    }
            } else {
                Toast.makeText(this, "Please enter email and password", Toast.LENGTH_SHORT).show()
            }
        }

        // Set sign-up prompt text click listener
        signUpPromptTextView.setOnClickListener {
            Toast.makeText(this, "Registration coming soon!", Toast.LENGTH_SHORT).show()
        }

        // Set sign-up as farmer button click listener
        signUpFarmerButton.setOnClickListener {
            val intent = Intent(this, RegistrationActivity::class.java)
            startActivity(intent)
        }

        // Set sign-up as expert button click listener
        signUpExpertButton.setOnClickListener {
            val intent = Intent(this, RegistrationActivity::class.java)
            startActivity(intent)
        }
    }

    private fun fetchUserRoleAndRedirect(userId: String) {
        // Initialize Firebase Database reference
        databaseReference = FirebaseDatabase.getInstance().reference.child("users").child(userId)

        // Fetch user role from database
        databaseReference.get().addOnSuccessListener { snapshot ->
            if (snapshot.exists()) {
                val role = snapshot.child("role").value.toString()
                when (role) {
                    "Expert" -> {
                        // Redirect to Expert Chat List (Dashboard)
                        val intent = Intent(this, ExpertChatListActivity::class.java)
                        startActivity(intent)
                        finish()
                    }
                    "Farmer" -> {
                        // Redirect to Main Activity
                        val intent = Intent(this, MainActivity::class.java)
                        startActivity(intent)
                        finish()
                    }
                    else -> {
                        Toast.makeText(this, "Unknown role. Please contact support.", Toast.LENGTH_SHORT).show()
                    }
                }
            } else {
                Toast.makeText(this, "User role not found.", Toast.LENGTH_SHORT).show()
            }
        }.addOnFailureListener { error ->
            Toast.makeText(this, "Failed to load user role: ${error.message}", Toast.LENGTH_SHORT).show()
        }
    }
}
