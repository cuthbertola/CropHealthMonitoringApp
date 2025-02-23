package com.example.crophealthmonitoringapp

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class WelcomeActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_welcome)

        // Initialize the buttons
        val signInButton = findViewById<Button>(R.id.signInButton)
        val createAccountTextView = findViewById<TextView>(R.id.createAccountTextView)

        // Set sign in button click listener
        signInButton.setOnClickListener {
            // Navigate to LoginActivity
            val loginIntent = Intent(this, LoginActivity::class.java)
            startActivity(loginIntent)
        }

        // Set create account click listener
        createAccountTextView.setOnClickListener {
            // Navigate to Registration Activity (to be implemented)
            // For now, we just show a placeholder
            val registrationIntent = Intent(this, RegistrationActivity::class.java)
            startActivity(registrationIntent)
        }
    }
}
