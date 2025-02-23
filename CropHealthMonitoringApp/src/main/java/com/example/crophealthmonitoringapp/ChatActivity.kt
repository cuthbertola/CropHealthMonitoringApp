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
    private lateinit var chatAdapter: ChatAdapter
    private var userId: String? = null
    private lateinit var chatId: String
    private var expertId: String? = null
    private var farmerId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize ViewBinding
        binding = ActivityChatBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Set up Toolbar
        setSupportActionBar(binding.chatToolbar)
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setDisplayShowHomeEnabled(true)
        }

        // Get current user ID
        val currentUser = FirebaseAuth.getInstance().currentUser
        userId = currentUser?.uid

        if (userId.isNullOrEmpty()) {
            Log.e("ChatActivity", "User not logged in or User ID is null!")
            Toast.makeText(this, "User not logged in!", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        // Retrieve chat details from Intent
        expertId = intent.getStringExtra("expertId")
        farmerId = intent.getStringExtra("farmerId")
        chatId = intent.getStringExtra("chatId") ?: generateChatId(
            farmerId ?: "",
            expertId ?: ""
        )

        // Initialize Firebase reference
        databaseReference = FirebaseDatabase.getInstance().reference.child("chats").child(chatId)

        // Ensure participants node is created (if it doesn't exist already)
        createParticipantsNode()

        // Setup Toolbar title
        setupToolbarTitle()

        // Initialize RecyclerView and load messages
        setupRecyclerView()
        loadChatMessages()

        // Set up Send Button Click Listener
        binding.buttonSend.setOnClickListener {
            val messageText = binding.editTextMessage.text.toString().trim()
            if (messageText.isNotEmpty()) {
                sendMessage(messageText)
            } else {
                Toast.makeText(this, "Message cannot be empty", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun setupToolbarTitle() {
        val title = if (userId == expertId) "Chat with Farmer" else "Chat with Expert"
        supportActionBar?.title = title
    }

    private fun setupRecyclerView() {
        binding.recyclerViewMessages.layoutManager = LinearLayoutManager(this)

        // Initialize the adapter
        chatAdapter = ChatAdapter(
            messages = mutableListOf(),
            chatSummaries = mutableListOf(),
            userId = userId!!,
            onChatClick = { chatId ->
                // Handle chat click if needed
            }
        )
        binding.recyclerViewMessages.adapter = chatAdapter
    }

    private fun sendMessage(messageText: String) {
        val messageId = databaseReference.child("messages").push().key ?: return
        val messageMap = mapOf(
            "messageText" to messageText,
            "senderId" to userId,
            "timestamp" to System.currentTimeMillis()
        )

        databaseReference.child("messages").child(messageId).setValue(messageMap)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    binding.editTextMessage.text.clear()
                    binding.recyclerViewMessages.scrollToPosition(chatAdapter.itemCount - 1)
                } else {
                    Toast.makeText(
                        this,
                        "Failed to send message: ${task.exception?.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
    }

    private fun loadChatMessages() {
        databaseReference.child("messages").orderByChild("timestamp").addChildEventListener(object : ChildEventListener {
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                val message = snapshot.getValue(Message::class.java)
                if (message != null) {
                    chatAdapter.addMessage(message)
                    binding.recyclerViewMessages.scrollToPosition(chatAdapter.itemCount - 1)
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

    private fun createParticipantsNode() {
        databaseReference.child("participants").setValue(
            mapOf(
                "expert_Id" to expertId,
                "user_Id" to farmerId
            )
        ).addOnCompleteListener { task ->
            if (!task.isSuccessful) {
                Log.e("ChatActivity", "Failed to create participants node: ${task.exception?.message}")
            }
        }
    }

    private fun generateChatId(farmerId: String, expertId: String): String {
        return if (farmerId < expertId) {
            "${farmerId}_$expertId"
        } else {
            "${expertId}_$farmerId"
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}
