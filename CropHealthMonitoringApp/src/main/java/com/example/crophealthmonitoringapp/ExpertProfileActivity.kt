package com.example.crophealthmonitoringapp

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.*

class ExpertProfileActivity : AppCompatActivity() {

    private lateinit var expertId: String
    private lateinit var databaseReference: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_expert_profile)

        // Retrieve expertId passed from the intent
        expertId = intent.getStringExtra("expertId") ?: ""
        if (expertId.isEmpty()) {
            Toast.makeText(this, "No Expert ID provided", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        Log.d("ExpertProfileActivity", "Expert ID: $expertId")

        // Initialize Firebase Database reference
        databaseReference = FirebaseDatabase.getInstance().reference.child("users").child(expertId)

        // Load the expert's details
        loadExpertDetails()

        // Set up the Chat button
        findViewById<Button>(R.id.chatWithExpertButton).setOnClickListener {
            val intent = Intent(this, ChatActivity::class.java)
            intent.putExtra("expertId", expertId)
            startActivity(intent)
        }
    }

    private fun loadExpertDetails() {
        databaseReference.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (!snapshot.exists()) {
                    Toast.makeText(this@ExpertProfileActivity, "Expert details not found", Toast.LENGTH_SHORT).show()
                    finish()
                    return
                }

                // Set data to TextViews with default values if a field is missing
                findViewById<TextView>(R.id.expertNameTextView).text =
                    snapshot.child("name").value?.toString() ?: "Name not available"
                findViewById<TextView>(R.id.expertSpecializationTextView).text =
                    "Specialization: ${snapshot.child("specialization").value?.toString() ?: "N/A"}"
                findViewById<TextView>(R.id.expertExperienceTextView).text =
                    "Experience: ${snapshot.child("experience").value?.toString() ?: "N/A"} years"
                findViewById<TextView>(R.id.expertCertificationsTextView).text =
                    "Certifications: ${snapshot.child("certifications/degree").value?.toString() ?: "N/A"}"
                findViewById<TextView>(R.id.expertBioTextView).text =
                    "Bio: ${snapshot.child("bio").value?.toString() ?: "N/A"}"
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("ExpertProfileActivity", "Database error: ${error.message}")
                Toast.makeText(this@ExpertProfileActivity, "Failed to load expert details: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }
}
