package com.example.crophealthmonitoringapp

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.crophealthmonitoringapp.databinding.ActivityLoginBinding
import com.google.firebase.auth.FirebaseAuth

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var auth: FirebaseAuth

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
                            // Login successful, navigate to MainActivity
                            Toast.makeText(this, "Login successful!", Toast.LENGTH_SHORT).show()
                            val intent = Intent(this, MainActivity::class.java)
                            startActivity(intent)
                            finish() // Close LoginActivity
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
            Toast.makeText(this, "Farmer registration coming soon!", Toast.LENGTH_SHORT).show()
            // Alternatively, navigate to a farmer registration activity:
            // val intent = Intent(this, FarmerRegistrationActivity::class.java)
            // startActivity(intent)
        }

        // Set sign-up as expert button click listener
        signUpExpertButton.setOnClickListener {
            Toast.makeText(this, "Expert registration coming soon!", Toast.LENGTH_SHORT).show()
            // Alternatively, navigate to an expert registration activity:
            // val intent = Intent(this, ExpertRegistrationActivity::class.java)
            // startActivity(intent)
        }
    }
}
