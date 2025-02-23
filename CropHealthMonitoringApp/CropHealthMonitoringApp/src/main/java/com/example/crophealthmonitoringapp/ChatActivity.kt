package com.example.crophealthmonitoringapp

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.crophealthmonitoringapp.databinding.ActivityChatBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class ChatActivity : AppCompatActivity() {

    private lateinit var binding: ActivityChatBinding
    private lateinit var databaseReference: DatabaseReference
    private lateinit var chatId: String
    private lateinit var chatAdapter: ChatAdapter
    private var userId: String? = null // Nullable for safety

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize ViewBinding
        binding = ActivityChatBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Retrieve the current user's ID from Firebase Authentication
        val currentUser = FirebaseAuth.getInstance().currentUser
        userId = currentUser?.uid

        // Log userId for debugging
        Log.d("ChatActivity", "Current User ID from FirebaseAuth: $userId")

        // Validate userId
        if (userId.isNullOrEmpty()) {
            Log.e("ChatActivity", "User not logged in or User ID is null!")
            Toast.makeText(this, "User not logged in!", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        // Retrieve the expertId passed from the Intent
        val expertId = intent.getStringExtra("expertId") ?: run {
            Log.e("ChatActivity", "Expert ID is missing!")
            Toast.makeText(this, "Expert ID is missing!", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        // Log expertId for debugging
        Log.d("ChatActivity", "Expert ID from Intent: $expertId")

        // Generate a unique chat ID
        chatId = generateChatId(userId!!, expertId)
        databaseReference = FirebaseDatabase.getInstance().reference
            .child("chats")
            .child(chatId)
            .child("messages")
        Log.d("ChatActivity", "Chat ID: $chatId")

        // Initialize RecyclerView and Adapter
        binding.recyclerViewMessages.layoutManager = LinearLayoutManager(this)
        chatAdapter = ChatAdapter(mutableListOf(), userId!!)
        binding.recyclerViewMessages.adapter = chatAdapter

        // Load chat messages
        loadChatMessages()

        // Set up the send button click listener
        binding.buttonSend.setOnClickListener {
            val messageText = binding.editTextMessage.text.toString().trim()
            if (messageText.isNotEmpty()) {
                sendMessage(messageText)
            } else {
                Toast.makeText(this, "Message cannot be empty", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun sendMessage(messageText: String) {
        if (userId.isNullOrEmpty()) {
            Log.e("ChatActivity", "User ID is null while sending message!")
            Toast.makeText(this, "Failed to send message: User ID is missing!", Toast.LENGTH_SHORT).show()
            return
        }

        val messageId = databaseReference.push().key ?: return
        val messageMap = mapOf(
            "messageText" to messageText,
            "senderId" to userId,
            "timestamp" to System.currentTimeMillis()
        )

        databaseReference.child(messageId).setValue(messageMap).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                binding.editTextMessage.text.clear()
                binding.recyclerViewMessages.scrollToPosition(chatAdapter.itemCount - 1)
            } else {
                Toast.makeText(this, "Failed to send message: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun loadChatMessages() {
        databaseReference.addChildEventListener(object : ChildEventListener {
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                val message = snapshot.getValue(Message::class.java) // Ensure the Message class is correct
                if (message != null) {
                    chatAdapter.addMessage(message)
                } else {
                    Log.e("ChatActivity", "Message is null or invalid!")
                }
            }

            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {}
            override fun onChildRemoved(snapshot: DataSnapshot) {}
            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {}
            override fun onCancelled(error: DatabaseError) {
                Log.e("ChatActivity", "Failed to load messages: ${error.message}")
                Toast.makeText(this@ChatActivity, "Failed to load messages: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun generateChatId(userId: String, expertId: String): String {
        // Removed underscore for compatibility
        return if (userId < expertId) {
            "$userId$expertId"
        } else {
            "$expertId$userId"
        }
    }
}
