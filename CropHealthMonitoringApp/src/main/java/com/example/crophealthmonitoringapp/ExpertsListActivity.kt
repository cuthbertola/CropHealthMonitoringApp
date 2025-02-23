package com.example.crophealthmonitoringapp

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.crophealthmonitoringapp.databinding.ActivityExpertsListBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class ExpertsListActivity : AppCompatActivity() {

    private lateinit var binding: ActivityExpertsListBinding
    private lateinit var expertsAdapter: ExpertsAdapter
    private lateinit var databaseReference: DatabaseReference
    private val expertsList = mutableListOf<Expert>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Check for user authentication
        if (FirebaseAuth.getInstance().currentUser == null) {
            redirectToLogin("User not authenticated. Please log in.")
            return
        }

        // Initialize ViewBinding
        binding = ActivityExpertsListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize RecyclerView
        setupRecyclerView()

        // Load experts from Firebase
        databaseReference = FirebaseDatabase.getInstance().reference.child("experts")
        loadExperts()
    }

    private fun setupRecyclerView() {
        binding.recyclerViewExperts.layoutManager = LinearLayoutManager(this)
        expertsAdapter = ExpertsAdapter(expertsList) { expertId ->
            openChatWithExpert(expertId) // Open chat with the selected expert
        }
        binding.recyclerViewExperts.adapter = expertsAdapter
    }

    private fun loadExperts() {
        // Show loading indicator
        showLoading(true)

        Log.d("ExpertsListActivity", "Starting to load experts from Firebase.")

        databaseReference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                expertsList.clear() // Clear the list to avoid duplicate entries

                if (snapshot.exists()) {
                    for (child in snapshot.children) {
                        val expert = child.getValue(Expert::class.java)
                        if (expert != null && expert.role == "expert") {
                            expertsList.add(expert)
                            Log.d("ExpertsListActivity", "Expert added: ${expert.name}")
                        }
                    }

                    if (expertsList.isEmpty()) {
                        Log.d("ExpertsListActivity", "No experts found.")
                        showNoExpertsMessage()
                    } else {
                        Log.d("ExpertsListActivity", "Experts loaded: ${expertsList.size}")
                        binding.recyclerViewExperts.visibility = View.VISIBLE
                        binding.textViewNoExperts.visibility = View.GONE
                        expertsAdapter.notifyDataSetChanged()
                    }
                } else {
                    showNoExpertsMessage()
                }

                showLoading(false)
            }

            override fun onCancelled(error: DatabaseError) {
                showLoading(false)
                Log.e("ExpertsListActivity", "Database error: ${error.message}")
                showErrorToast(error)
            }
        })
    }

    private fun openChatWithExpert(expertId: String) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId != null) {
            // Generate a unique chat ID with consistent format
            val chatId = generateChatId(userId, expertId)
            val intent = Intent(this, ChatActivity::class.java).apply {
                putExtra("chatId", chatId)
                putExtra("expertId", expertId) // Pass the expertId to ChatActivity
                putExtra("farmerId", userId)  // Pass the farmerId (current user)
            }
            startActivity(intent)
        } else {
            Toast.makeText(this, "Error: Unable to identify the user.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun generateChatId(farmerId: String, expertId: String): String {
        return if (farmerId < expertId) {
            "${farmerId}_$expertId"
        } else {
            "${expertId}_$farmerId"
        }
    }

    private fun showLoading(isLoading: Boolean) {
        if (isLoading) {
            binding.progressBar.visibility = View.VISIBLE
            binding.recyclerViewExperts.visibility = View.GONE
            binding.textViewNoExperts.visibility = View.GONE
        } else {
            binding.progressBar.visibility = View.GONE
        }
    }

    private fun showNoExpertsMessage() {
        binding.textViewNoExperts.visibility = View.VISIBLE
        binding.recyclerViewExperts.visibility = View.GONE
    }

    private fun showErrorToast(error: DatabaseError) {
        val errorMessage = when (error.code) {
            DatabaseError.PERMISSION_DENIED -> "Access denied. Please check your permissions."
            DatabaseError.NETWORK_ERROR -> "Network error. Please check your internet connection."
            else -> "Failed to load experts: ${error.message}"
        }
        Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show()
    }

    private fun redirectToLogin(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}
